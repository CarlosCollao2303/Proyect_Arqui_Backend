package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.ExamenesDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IExamenesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
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

    @PostMapping("/Registrar")
    public ResponseEntity<ExamenesDTO> registrar(@Valid @RequestBody ExamenesDTO dto) {
        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Examenes examen = new Examenes();
        examen.setDetalleHistorial(detalle);
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

        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Examenes examen = existente.get();
        examen.setDetalleHistorial(detalle);
        examen.setTipoExamen(dto.getTipoExamen());
        examen.setResultado(dto.getResultado());
        examen.setFechaSolicitud(dto.getFechaSolicitud());
        examen.setFechaResultado(dto.getFechaResultado());

        eS.update(examen);

        ExamenesDTO responseDTO = convertirADTO(examen);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Examenes examen = eS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un examen con el id: " + id
                        )
                );

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
        dto.setIdDetalleHistorial(examen.getDetalleHistorial().getIdDetalleHistorial());
        dto.setTipoExamen(examen.getTipoExamen());
        dto.setResultado(examen.getResultado());
        dto.setFechaSolicitud(examen.getFechaSolicitud());
        dto.setFechaResultado(examen.getFechaResultado());
        return dto;
    }
}
