package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.UsuariosDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IEspecialidadesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRolesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuariosController {

    private final IUsuariosService uS;
    private final IRolesService rS;
    private final IEspecialidadesService eS;
    private final PasswordEncoder passwordEncoder;

    public UsuariosController(IUsuariosService uS, IRolesService rS, IEspecialidadesService eS,
                              PasswordEncoder passwordEncoder) {
        this.uS = uS;
        this.rS = rS;
        this.eS = eS;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MEDICO')")
    @GetMapping("/Listar")
    public ResponseEntity<List<UsuariosDTO>> listar() {
        List<UsuariosDTO> lista = uS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // ADMIN y MEDICO ven a cualquiera; un PACIENTE solo a si mismo.
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<UsuariosDTO> listarPorId(@PathVariable int id, Authentication auth) {
        Usuarios usuario = uS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un usuario con el id: " + id
                        )
                );

        if (!tieneRol(auth, "ADMIN") && !tieneRol(auth, "MEDICO")) {
            verificarQueEsElMismo(usuario, auth);
        }

        return ResponseEntity.ok(convertirADTO(usuario));
    }

    // Endpoint publico (ver SecurityConfig). Si lo llama un ADMIN con su token puede
    // elegir rol y especialidad; cualquier otro se registra siempre como PACIENTE,
    // asi nadie puede auto-asignarse ADMIN o MEDICO mandando otro idRol.
    @PostMapping("/Registrar")
    public ResponseEntity<UsuariosDTO> registrar(@Valid @RequestBody UsuariosDTO dto, Authentication auth) {
        // La contrasena no lleva @NotBlank en el DTO (el PUT puede omitirla),
        // asi que al registrar se exige aqui para no llamar a encode(null).
        if (dto.getContrasenaHash() == null || dto.getContrasenaHash().isBlank()) {
            throw new BadRequestException("La contrasena es obligatoria para registrar un usuario");
        }

        boolean esAdmin = tieneRol(auth, "ADMIN");

        Roles rol;
        Especialidades especialidad;
        if (esAdmin) {
            rol = obtenerRol(dto.getIdRol());
            especialidad = obtenerEspecialidad(dto.getIdEspecialidad());
        } else {
            rol = rS.listByNombre("PACIENTE")
                    .orElseThrow(() ->
                            new ResourceNotFoundException("No existe el rol PACIENTE")
                    );
            especialidad = null;
        }

        Usuarios usuario = new Usuarios();
        usuario.setRol(rol);
        usuario.setEspecialidad(especialidad);
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setDni(dto.getDni());
        usuario.setTelefono(dto.getTelefono());
        usuario.setCorreo(dto.getCorreo());
        usuario.setContrasenaHash(passwordEncoder.encode(dto.getContrasenaHash()));
        usuario.setColegiatura(dto.getColegiatura());
        usuario.setRegionUbicacion(dto.getRegionUbicacion());
        // Un auto-registro queda activo; solo el ADMIN puede crear cuentas desactivadas.
        usuario.setEstado(esAdmin ? dto.isEstado() : true);

        uS.insert(usuario);

        UsuariosDTO responseDTO = convertirADTO(usuario);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(usuario.getIdUsuario())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    // ADMIN edita a cualquiera. Los demas solo su propio usuario, y sin poder
    // cambiarse rol, especialidad ni estado.
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/Actualizar")
    public ResponseEntity<UsuariosDTO> actualizar(@Valid @RequestBody UsuariosDTO dto, Authentication auth) {
        Optional<Usuarios> existente = uS.listId(dto.getIdUsuario());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un usuario con el id: " + dto.getIdUsuario()
            );
        }

        Usuarios usuario = existente.get();
        boolean esAdmin = tieneRol(auth, "ADMIN");

        if (esAdmin) {
            usuario.setRol(obtenerRol(dto.getIdRol()));
            usuario.setEspecialidad(obtenerEspecialidad(dto.getIdEspecialidad()));
            usuario.setEstado(dto.isEstado());
        } else {
            verificarQueEsElMismo(usuario, auth);
        }

        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setDni(dto.getDni());
        usuario.setTelefono(dto.getTelefono());
        usuario.setCorreo(dto.getCorreo());

        // Solo se re-encripta si el cliente mando una contrasena nueva; si viene vacia
        // se conserva la que ya tenia. Sin esto, un PUT normal re-hashearia el hash.
        if (dto.getContrasenaHash() != null && !dto.getContrasenaHash().isBlank()) {
            usuario.setContrasenaHash(passwordEncoder.encode(dto.getContrasenaHash()));
        }

        usuario.setColegiatura(dto.getColegiatura());
        usuario.setRegionUbicacion(dto.getRegionUbicacion());

        uS.update(usuario);

        UsuariosDTO responseDTO = convertirADTO(usuario);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Usuarios usuario = uS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un usuario con el id: " + id
                        )
                );

        uS.delete(usuario.getIdUsuario());
        return ResponseEntity.noContent().build();
    }

    private Roles obtenerRol(Integer idRol) {
        if (idRol == null) {
            throw new BadRequestException("El id del rol es obligatorio");
        }

        return rS.listId(idRol)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el rol con el id: " + idRol
                        )
                );
    }

    // auth es null cuando la peticion no trae token (registro publico).
    private boolean tieneRol(Authentication auth, String rol) {
        return auth != null && auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }

    // El subject del JWT es el correo (ver JwtTokenService).
    private void verificarQueEsElMismo(Usuarios usuario, Authentication auth) {
        if (!usuario.getCorreo().equals(auth.getName())) {
            throw new AccessDeniedException("Solo puedes acceder a tu propio usuario");
        }
    }

    private Especialidades obtenerEspecialidad(Integer idEspecialidad) {
        if (idEspecialidad == null) {
            return null;
        }

        return eS.listId(idEspecialidad)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la especialidad con el id: " + idEspecialidad
                        )
                );
    }

    private UsuariosDTO convertirADTO(Usuarios usuario) {
        UsuariosDTO dto = new UsuariosDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setIdRol(usuario.getRol().getIdRol());
        dto.setIdEspecialidad(
                usuario.getEspecialidad() != null
                        ? usuario.getEspecialidad().getIdEspecialidad()
                        : null
        );
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setDni(usuario.getDni());
        dto.setTelefono(usuario.getTelefono());
        dto.setCorreo(usuario.getCorreo());
        // La contrasena no se copia al DTO: es WRITE_ONLY y nunca sale en las respuestas.
        dto.setColegiatura(usuario.getColegiatura());
        dto.setRegionUbicacion(usuario.getRegionUbicacion());
        dto.setEstado(usuario.isEstado());
        return dto;
    }
}
