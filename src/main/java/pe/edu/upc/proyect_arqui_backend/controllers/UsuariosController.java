package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.UsuariosDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IEspecialidadesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRolesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuariosController {

    // Ojo: el proyecto NO usa el prefijo ROLE_, la autoridad es el nombre crudo
    // de la columna roles.nombre (ver JwtUserDetailsService).
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final IUsuariosService uS;
    private final IRolesService rS;
    private final IEspecialidadesService eS;

    public UsuariosController(IUsuariosService uS, IRolesService rS, IEspecialidadesService eS) {
        this.uS = uS;
        this.rS = rS;
        this.eS = eS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<UsuariosDTO>> listar() {
        List<UsuariosDTO> lista = uS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PostMapping("/Registrar")
    public ResponseEntity<UsuariosDTO> registrar(@Valid @RequestBody UsuariosDTO dto) {
        Roles rol = rS.listId(dto.getIdRol())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el rol con el id: " + dto.getIdRol()
                        )
                );

        Especialidades especialidad = obtenerEspecialidad(dto.getIdEspecialidad());

        Usuarios usuario = new Usuarios();
        usuario.setRol(rol);
        usuario.setEspecialidad(especialidad);
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setDni(dto.getDni());
        usuario.setTelefono(dto.getTelefono());
        usuario.setCorreo(dto.getCorreo());
        usuario.setContrasenaHash(dto.getContrasenaHash());
        usuario.setColegiatura(dto.getColegiatura());
        usuario.setRegionUbicacion(dto.getRegionUbicacion());
        usuario.setEstado(dto.isEstado());

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

    @PutMapping("/Actualizar")
    public ResponseEntity<UsuariosDTO> actualizar(@Valid @RequestBody UsuariosDTO dto) {
        Optional<Usuarios> existente = uS.listId(dto.getIdUsuario());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un usuario con el id: " + dto.getIdUsuario()
            );
        }

        Roles rol = rS.listId(dto.getIdRol())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el rol con el id: " + dto.getIdRol()
                        )
                );

        Especialidades especialidad = obtenerEspecialidad(dto.getIdEspecialidad());

        Usuarios usuario = existente.get();
        usuario.setRol(rol);
        usuario.setEspecialidad(especialidad);
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setDni(dto.getDni());
        usuario.setTelefono(dto.getTelefono());
        usuario.setCorreo(dto.getCorreo());
        usuario.setContrasenaHash(dto.getContrasenaHash());
        usuario.setColegiatura(dto.getColegiatura());
        usuario.setRegionUbicacion(dto.getRegionUbicacion());
        usuario.setEstado(dto.isEstado());

        uS.update(usuario);

        UsuariosDTO responseDTO = convertirADTO(usuario);

        return ResponseEntity.ok(responseDTO);
    }

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

    private boolean esAdministrador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROL_ADMINISTRADOR::equals);
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

        // La contrasena solo viaja de vuelta si el solicitante es ADMINISTRADOR
        if (esAdministrador()) {
            dto.setContrasenaHash(usuario.getContrasenaHash());
        }

        dto.setColegiatura(usuario.getColegiatura());
        dto.setRegionUbicacion(usuario.getRegionUbicacion());
        dto.setEstado(usuario.isEstado());
        return dto;
    }
}
