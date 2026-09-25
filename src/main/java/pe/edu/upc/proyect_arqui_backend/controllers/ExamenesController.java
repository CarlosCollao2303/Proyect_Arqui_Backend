package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
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

    @PostMapping("/Registrar")
    public ResponseEntity<ExamenesDTO> registrar(@Valid @RequestBody ExamenesDTO dto) {
        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Examenes examen = new Examenes();
        examen.setTipoExamen(dto.getTipoExamen());
        examen.setResultado(dto.getResultado());
        examen.setFechaSolicitud(dto.getFechaSolicitud());
        examen.setFechaResultado(dto.getFechaResultado());

        eS.insert(examen);
        detalle.setExamen(examen);
        dhS.update(detalle);

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

        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Examenes examen = existente.get();
        examen.setTipoExamen(dto.getTipoExamen());
        examen.setResultado(dto.getResultado());
        examen.setFechaSolicitud(dto.getFechaSolicitud());
        examen.setFechaResultado(dto.getFechaResultado());

        eS.update(examen);
        dhS.findByExamenId(examen.getIdExamen()).ifPresent(anterior -> {
            if (anterior.getIdDetalleHistorial() != detalle.getIdDetalleHistorial()) {
                anterior.setExamen(null);
                dhS.update(anterior);
            }
        });
        detalle.setExamen(examen);
        dhS.update(detalle);

        ExamenesDTO responseDTO = convertirADTO(examen);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Examenes examen = eS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un examen con el id: " + id
                        )
                );

        dhS.findByExamenId(examen.getIdExamen()).ifPresent(detalle -> {
            detalle.setExamen(null);
            dhS.update(detalle);
        });
        eS.delete(examen.getIdExamen());
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

    private ExamenesDTO convertirADTO(Examenes examen) {
        ExamenesDTO dto = new ExamenesDTO();
        dto.setIdExamen(examen.getIdExamen());
        dto.setIdDetalleHistorial(dhS.findByExamenId(examen.getIdExamen())
                .map(DetalleHistorial::getIdDetalleHistorial)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el detalle de historial asociado al examen con el id: " + examen.getIdExamen()
                )));
        dto.setTipoExamen(examen.getTipoExamen());
        dto.setResultado(examen.getResultado());
        dto.setFechaSolicitud(examen.getFechaSolicitud());
        dto.setFechaResultado(examen.getFechaResultado());
        return dto;
    }
}
