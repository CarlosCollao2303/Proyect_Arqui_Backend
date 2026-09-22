package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.TratamientosDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ITratamientosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tratamientos")
public class TratamientosController {

    private final ITratamientosService tS;
    private final IDetalleHistorialService dhS;

    public TratamientosController(ITratamientosService tS, IDetalleHistorialService dhS) {
        this.tS = tS;
        this.dhS = dhS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<TratamientosDTO>> listar() {
        List<TratamientosDTO> lista = tS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<TratamientosDTO> listarPorId(@PathVariable int id) {
        Tratamientos tratamiento = tS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un tratamiento con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(tratamiento));
    }

    @PostMapping("/Registrar")
    public ResponseEntity<TratamientosDTO> registrar(@Valid @RequestBody TratamientosDTO dto) {
        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Tratamientos tratamiento = new Tratamientos();
        tratamiento.setDetalleHistorial(detalle);
        tratamiento.setDescripcion(dto.getDescripcion());
        tratamiento.setFechaInicio(dto.getFechaInicio());
        tratamiento.setFechaFin(dto.getFechaFin());
        tratamiento.setEstado(dto.getEstado());

        tS.insert(tratamiento);

        TratamientosDTO responseDTO = convertirADTO(tratamiento);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tratamiento.getIdTratamiento())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    public ResponseEntity<TratamientosDTO> actualizar(@Valid @RequestBody TratamientosDTO dto) {
        Optional<Tratamientos> existente = tS.listId(dto.getIdTratamiento());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un tratamiento con el id: " + dto.getIdTratamiento()
            );
        }

        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Tratamientos tratamiento = existente.get();
        tratamiento.setDetalleHistorial(detalle);
        tratamiento.setDescripcion(dto.getDescripcion());
        tratamiento.setFechaInicio(dto.getFechaInicio());
        tratamiento.setFechaFin(dto.getFechaFin());
        tratamiento.setEstado(dto.getEstado());

        tS.update(tratamiento);

        TratamientosDTO responseDTO = convertirADTO(tratamiento);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Tratamientos tratamiento = tS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un tratamiento con el id: " + id
                        )
                );

        tS.delete(tratamiento.getIdTratamiento());
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

    private TratamientosDTO convertirADTO(Tratamientos tratamiento) {
        TratamientosDTO dto = new TratamientosDTO();
        dto.setIdTratamiento(tratamiento.getIdTratamiento());
        dto.setIdDetalleHistorial(tratamiento.getDetalleHistorial().getIdDetalleHistorial());
        dto.setDescripcion(tratamiento.getDescripcion());
        dto.setFechaInicio(tratamiento.getFechaInicio());
        dto.setFechaFin(tratamiento.getFechaFin());
        dto.setEstado(tratamiento.getEstado());
        return dto;
    }
}
