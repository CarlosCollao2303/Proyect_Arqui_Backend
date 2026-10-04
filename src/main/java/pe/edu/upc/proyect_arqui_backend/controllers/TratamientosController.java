package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.TratamientosDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ITratamientosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAnyAuthority('ADMIN','MEDICO')")
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

    @GetMapping("/BuscarPorEstado")
    public ResponseEntity<List<TratamientosDTO>> buscarPorEstado(@RequestParam String estado) {
        if (estado.isBlank()) {
            throw new BadRequestException("El estado del tratamiento no puede estar vacio");
        }

        List<TratamientosDTO> lista = tS.listByEstado(estado.trim())
                .stream()
                .map(this::convertirADTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/BuscarPorFechaInicio")
    public ResponseEntity<List<TratamientosDTO>> buscarPorFechaInicio(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fin) {
        if (inicio.isAfter(fin)) {
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        List<TratamientosDTO> lista = tS.listByFechaInicioBetween(inicio, fin)
                .stream()
                .map(this::convertirADTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/Registrar")
    public ResponseEntity<TratamientosDTO> registrar(@Valid @RequestBody TratamientosDTO dto) {
        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Tratamientos tratamiento = new Tratamientos();
        tratamiento.setDescripcion(dto.getDescripcion());
        tratamiento.setFechaInicio(dto.getFechaInicio());
        tratamiento.setFechaFin(dto.getFechaFin());
        tratamiento.setEstado(dto.getEstado());

        tS.insert(tratamiento);
        detalle.setTratamiento(tratamiento);
        dhS.update(detalle);

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
        tratamiento.setDescripcion(dto.getDescripcion());
        tratamiento.setFechaInicio(dto.getFechaInicio());
        tratamiento.setFechaFin(dto.getFechaFin());
        tratamiento.setEstado(dto.getEstado());

        tS.update(tratamiento);
        dhS.findAllByTratamientoId(tratamiento.getIdTratamiento()).forEach(anterior -> {
            if (anterior.getIdDetalleHistorial() != detalle.getIdDetalleHistorial()) {
                anterior.setTratamiento(null);
                dhS.update(anterior);
            }
        });
        detalle.setTratamiento(tratamiento);
        dhS.update(detalle);

        TratamientosDTO responseDTO = convertirADTO(tratamiento);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Tratamientos tratamiento = tS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un tratamiento con el id: " + id
                        )
                );

        dhS.findAllByTratamientoId(tratamiento.getIdTratamiento()).forEach(detalle -> {
            detalle.setTratamiento(null);
            dhS.update(detalle);
        });
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
        Integer idDetalleHistorial = dhS.findAllByTratamientoId(tratamiento.getIdTratamiento()).stream()
                .map(DetalleHistorial::getIdDetalleHistorial)
                .findFirst()
                .orElse(null);
        dto.setIdDetalleHistorial(idDetalleHistorial);
        dto.setDescripcion(tratamiento.getDescripcion());
        dto.setFechaInicio(tratamiento.getFechaInicio());
        dto.setFechaFin(tratamiento.getFechaFin());
        dto.setEstado(tratamiento.getEstado());
        return dto;
    }
}
