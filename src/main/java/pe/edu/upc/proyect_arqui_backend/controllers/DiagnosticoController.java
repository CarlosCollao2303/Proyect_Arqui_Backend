package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.DiagnosticoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Diagnostico;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDiagnosticoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAnyAuthority('ADMIN','MEDICO')")
@RequestMapping("/diagnosticos")
public class DiagnosticoController {

    private final IDiagnosticoService dS;
    private final IDetalleHistorialService dhS;

    public DiagnosticoController(IDiagnosticoService dS, IDetalleHistorialService dhS) {
        this.dS = dS;
        this.dhS = dhS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<DiagnosticoDTO>> listar() {
        List<DiagnosticoDTO> lista = dS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<DiagnosticoDTO> listarPorId(@PathVariable int id) {
        Diagnostico diagnostico = dS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un diagnostico con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(diagnostico));
    }

    @PostMapping("/Registrar")
    public ResponseEntity<DiagnosticoDTO> registrar(@Valid @RequestBody DiagnosticoDTO dto) {
        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setCodigoCie10(dto.getCodigoCie10());
        diagnostico.setDescripcion(dto.getDescripcion());
        diagnostico.setFechaRegistro(dto.getFechaRegistro());

        dS.insert(diagnostico);
        detalle.setDiagnostico(diagnostico);
        dhS.update(detalle);

        DiagnosticoDTO responseDTO = convertirADTO(diagnostico);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(diagnostico.getIdDiagnostico())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    public ResponseEntity<DiagnosticoDTO> actualizar(@Valid @RequestBody DiagnosticoDTO dto) {
        Optional<Diagnostico> existente = dS.listId(dto.getIdDiagnostico());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un diagnostico con el id: " + dto.getIdDiagnostico()
            );
        }

        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Diagnostico diagnostico = existente.get();
        diagnostico.setCodigoCie10(dto.getCodigoCie10());
        diagnostico.setDescripcion(dto.getDescripcion());
        diagnostico.setFechaRegistro(dto.getFechaRegistro());

        dS.update(diagnostico);
        dhS.findByDiagnosticoId(diagnostico.getIdDiagnostico()).ifPresent(anterior -> {
            if (anterior.getIdDetalleHistorial() != detalle.getIdDetalleHistorial()) {
                anterior.setDiagnostico(null);
                dhS.update(anterior);
            }
        });
        detalle.setDiagnostico(diagnostico);
        dhS.update(detalle);

        DiagnosticoDTO responseDTO = convertirADTO(diagnostico);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Diagnostico diagnostico = dS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un diagnostico con el id: " + id
                        )
                );

        dhS.findByDiagnosticoId(diagnostico.getIdDiagnostico()).ifPresent(detalle -> {
            detalle.setDiagnostico(null);
            dhS.update(detalle);
        });
        dS.delete(diagnostico.getIdDiagnostico());
        return ResponseEntity.noContent().build();
    }

    private DetalleHistorial obtenerDetalle(Integer idDetalleHistorial) {
        return dhS.listId(idDetalleHistorial)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el detalle de historial con el id: " + idDetalleHistorial
                        )
                );
    }

    private DiagnosticoDTO convertirADTO(Diagnostico diagnostico) {
        DiagnosticoDTO dto = new DiagnosticoDTO();
        dto.setIdDiagnostico(diagnostico.getIdDiagnostico());
        dto.setIdDetalleHistorial(dhS.findByDiagnosticoId(diagnostico.getIdDiagnostico())
                .map(DetalleHistorial::getIdDetalleHistorial)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el detalle de historial asociado al diagnostico con el id: " + diagnostico.getIdDiagnostico()
                )));
        dto.setCodigoCie10(diagnostico.getCodigoCie10());
        dto.setDescripcion(diagnostico.getDescripcion());
        dto.setFechaRegistro(diagnostico.getFechaRegistro());
        return dto;
    }
}
