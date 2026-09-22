package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.RolesDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRolesService;
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

    public RolesController(IRolesService rS) {
        this.rS = rS;
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
        Roles rol = new Roles();
        rol.setNombre(dto.getNombre());

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
        rol.setNombre(dto.getNombre());

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

        rS.delete(rol.getIdRol());
        return ResponseEntity.noContent().build();
    }

    private RolesDTO convertirADTO(Roles rol) {
        RolesDTO dto = new RolesDTO();
        dto.setIdRol(rol.getIdRol());
        dto.setNombre(rol.getNombre());
        return dto;
    }
}
