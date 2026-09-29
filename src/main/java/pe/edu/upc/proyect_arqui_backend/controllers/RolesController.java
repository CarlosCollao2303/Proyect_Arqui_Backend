package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.RolesDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRolesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/roles")
public class RolesController {

    private final IRolesService rS;
    private final IUsuariosService uS;

    public RolesController(IRolesService rS, IUsuariosService uS) {
        this.rS = rS;
        this.uS = uS;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/Listar")
    public ResponseEntity<List<RolesDTO>> listar() {
        List<RolesDTO> lista = rS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<RolesDTO> listarPorId(@PathVariable int id) {
        Roles rol = rS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un rol con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(rol));
    }

    @PostMapping("/Registrar")
    public ResponseEntity<RolesDTO> registrar(@Valid @RequestBody RolesDTO dto) {
        String nombre = normalizarNombre(dto.getNombre());

        if (rS.listByNombre(nombre).isPresent()) {
            throw new BadRequestException("Ya existe un rol con el nombre: " + nombre);
        }

        Roles rol = new Roles();
        rol.setNombre(nombre);

        rS.insert(rol);

        RolesDTO responseDTO = convertirADTO(rol);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(rol.getIdRol())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    public ResponseEntity<RolesDTO> actualizar(@Valid @RequestBody RolesDTO dto) {
        Optional<Roles> existente = rS.listId(dto.getIdRol());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un rol con el id: " + dto.getIdRol()
            );
        }

        Roles rol = existente.get();
        String nombre = normalizarNombre(dto.getNombre());

        if (!nombre.equals(rol.getNombre())) {
            verificarQueNoEsRolBase(rol, "renombrar");

            if (rS.listByNombre(nombre).isPresent()) {
                throw new BadRequestException("Ya existe un rol con el nombre: " + nombre);
            }
        }

        rol.setNombre(nombre);

        rS.update(rol);

        RolesDTO responseDTO = convertirADTO(rol);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Roles rol = rS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un rol con el id: " + id
                        )
                );

        verificarQueNoEsRolBase(rol, "eliminar");

        if (uS.existsByRol(rol.getIdRol())) {
            throw new BadRequestException(
                    "No se puede eliminar el rol " + rol.getNombre() + " porque tiene usuarios asignados"
            );
        }

        rS.delete(rol.getIdRol());
        return ResponseEntity.noContent().build();
    }

    // Los @PreAuthorize comparan el nombre exacto ('ADMIN', 'MEDICO'...), asi que se
    // guarda siempre en mayusculas. La coma se rechaza porque el claim "roles" del JWT
    // se separa por comas (ver CustomJwtAuthenticationConverter).
    private String normalizarNombre(String nombre) {
        String normalizado = nombre.trim().toUpperCase();

        if (normalizado.contains(",")) {
            throw new BadRequestException("El nombre del rol no puede contener comas");
        }

        return normalizado;
    }

    private void verificarQueNoEsRolBase(Roles rol, String accion) {
        if (Roles.ROLES_BASE.contains(rol.getNombre())) {
            throw new BadRequestException(
                    "El rol " + rol.getNombre() + " es del sistema y no se puede " + accion
            );
        }
    }

    private RolesDTO convertirADTO(Roles rol) {
        RolesDTO dto = new RolesDTO();
        dto.setIdRol(rol.getIdRol());
        dto.setNombre(rol.getNombre());
        return dto;
    }
}
