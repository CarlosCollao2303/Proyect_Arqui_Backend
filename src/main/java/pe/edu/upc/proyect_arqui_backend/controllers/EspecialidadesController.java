package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.EspecialidadesDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IEspecialidadesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/especialidades")
public class EspecialidadesController {

    private final IEspecialidadesService eS;

    public EspecialidadesController(IEspecialidadesService eS) {
        this.eS = eS;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/Listar")
    public ResponseEntity<List<EspecialidadesDTO>> listar() {
        List<EspecialidadesDTO> lista = eS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<EspecialidadesDTO> listarPorId(@PathVariable int id) {
        Especialidades especialidad = eS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una especialidad con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(especialidad));
    }

    @PostMapping("/Registrar")
    public ResponseEntity<EspecialidadesDTO> registrar(@Valid @RequestBody EspecialidadesDTO dto) {
        Especialidades especialidad = new Especialidades();
        especialidad.setNombre(dto.getNombre());

        eS.insert(especialidad);

        EspecialidadesDTO responseDTO = convertirADTO(especialidad);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(especialidad.getIdEspecialidad())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    public ResponseEntity<EspecialidadesDTO> actualizar(@Valid @RequestBody EspecialidadesDTO dto) {
        Optional<Especialidades> existente = eS.listId(dto.getIdEspecialidad());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una especialidad con el id: " + dto.getIdEspecialidad()
            );
        }

        Especialidades especialidad = existente.get();
        especialidad.setNombre(dto.getNombre());

        eS.update(especialidad);

        EspecialidadesDTO responseDTO = convertirADTO(especialidad);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Especialidades especialidad = eS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una especialidad con el id: " + id
                        )
                );

        eS.delete(especialidad.getIdEspecialidad());
        return ResponseEntity.noContent().build();
    }

    private EspecialidadesDTO convertirADTO(Especialidades especialidad) {
        EspecialidadesDTO dto = new EspecialidadesDTO();
        dto.setIdEspecialidad(especialidad.getIdEspecialidad());
        dto.setNombre(especialidad.getNombre());
        return dto;
    }
}
