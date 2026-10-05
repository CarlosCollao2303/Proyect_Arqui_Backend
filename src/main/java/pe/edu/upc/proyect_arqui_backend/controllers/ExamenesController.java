package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.CantidadExamenesDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.ExamenPendienteDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.ExamenesDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IExamenesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAnyAuthority('ADMIN','MEDICO')")
@RequestMapping("/examenes")
public class ExamenesController {

    private final IExamenesService eS;
    private final IDetalleHistorialService dhS;

    public ExamenesController(IExamenesService eS, IDetalleHistorialService dhS) {
        this.eS = eS;
        this.dhS = dhS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<ExamenesDTO>> listar() {
        List<ExamenesDTO> lista = eS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<ExamenesDTO> listarPorId(@PathVariable int id) {
        Examenes examen = eS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un examen con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(examen));
    }

    @GetMapping("/Buscar")
    public ResponseEntity<List<ExamenesDTO>> buscarPorTipo(@RequestParam String tipoExamen) {
        List<ExamenesDTO> lista = eS.listByTipoExamen(tipoExamen)
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }
    @GetMapping("/cantidad-por-paciente")
    public ResponseEntity<List<CantidadExamenesDTO>> cantidadPorPaciente() {
        return ResponseEntity.ok(eS.contarExamenesPorPaciente());
    }

    @GetMapping("/cantidad-por-paciente/{idPaciente}")
    public ResponseEntity<CantidadExamenesDTO> cantidadDePaciente(@PathVariable int idPaciente) {
        return ResponseEntity.ok(eS.contarExamenesDePaciente(idPaciente));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<ExamenPendienteDTO>> pendientes() {
        return ResponseEntity.ok(eS.listarExamenesPendientes());
    }

    @PostMapping("/Registrar")
    public ResponseEntity<ExamenesDTO> registrar(@Valid @RequestBody ExamenesDTO dto) {
        Examenes examen = new Examenes();
        examen.setTipoExamen(dto.getTipoExamen());
        examen.setResultado(dto.getResultado());
        examen.setFechaSolicitud(dto.getFechaSolicitud());
        examen.setFechaResultado(dto.getFechaResultado());

        eS.insert(examen);

        ExamenesDTO responseDTO = convertirADTO(examen);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(examen.getIdExamen())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    public ResponseEntity<ExamenesDTO> actualizar(@Valid @RequestBody ExamenesDTO dto) {
        Optional<Examenes> existente = eS.listId(dto.getIdExamen());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un examen con el id: " + dto.getIdExamen()
            );
        }

        Examenes examen = existente.get();
        examen.setTipoExamen(dto.getTipoExamen());
        examen.setResultado(dto.getResultado());
        examen.setFechaSolicitud(dto.getFechaSolicitud());
        examen.setFechaResultado(dto.getFechaResultado());

        eS.update(examen);

        ExamenesDTO responseDTO = convertirADTO(examen);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Examenes examen = eS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un examen con el id: " + id
                        )
                );

        dhS.findAllByExamenId(examen.getIdExamen()).forEach(detalle -> {
            detalle.setExamen(null);
            dhS.update(detalle);
        });
        eS.delete(examen.getIdExamen());
        return ResponseEntity.noContent().build();
    }

    private ExamenesDTO convertirADTO(Examenes examen) {
        ExamenesDTO dto = new ExamenesDTO();
        dto.setIdExamen(examen.getIdExamen());
        dto.setTipoExamen(examen.getTipoExamen());
        dto.setResultado(examen.getResultado());
        dto.setFechaSolicitud(examen.getFechaSolicitud());
        dto.setFechaResultado(examen.getFechaResultado());
        return dto;
    }
}
