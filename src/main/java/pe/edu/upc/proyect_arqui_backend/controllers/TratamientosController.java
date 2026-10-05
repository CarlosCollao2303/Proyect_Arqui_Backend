package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.TratamientoPacienteDTO;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;

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
            @RequestParam LocalDate fecha) {
        List<TratamientosDTO> lista = tS.listByFechaInicio(fecha)
                .stream()
                .map(this::convertirADTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    // Si no se envia la fecha, se toman los tratamientos vigentes hoy.
    @GetMapping("/ListarVigentes")
    public ResponseEntity<List<TratamientosDTO>> listarVigentes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        List<TratamientosDTO> lista = tS.listarTratamientosVigentes(dia)
                .stream()
                .map(this::convertirADTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ListarPorPaciente/{idPaciente}")
    public ResponseEntity<List<TratamientoPacienteDTO>> listarPorPaciente(@PathVariable int idPaciente) {
        return ResponseEntity.ok(tS.listarTratamientosDePaciente(idPaciente));
    }

    @PostMapping("/Registrar")
    public ResponseEntity<TratamientosDTO> registrar(@Valid @RequestBody TratamientosDTO dto) {
        validarFechas(dto);

        Tratamientos tratamiento = new Tratamientos();
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
        validarFechas(dto);

        Optional<Tratamientos> existente = tS.listId(dto.getIdTratamiento());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un tratamiento con el id: " + dto.getIdTratamiento()
            );
        }

        Tratamientos tratamiento = existente.get();
        tratamiento.setDescripcion(dto.getDescripcion());
        tratamiento.setFechaInicio(dto.getFechaInicio());
        tratamiento.setFechaFin(dto.getFechaFin());
        tratamiento.setEstado(dto.getEstado());

        tS.update(tratamiento);

        TratamientosDTO responseDTO = convertirADTO(tratamiento);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    @Transactional
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

    // Las dos fechas ya vienen con @NotNull; aqui solo se valida el orden.
    private void validarFechas(TratamientosDTO dto) {
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

    private TratamientosDTO convertirADTO(Tratamientos tratamiento) {
        TratamientosDTO dto = new TratamientosDTO();
        dto.setIdTratamiento(tratamiento.getIdTratamiento());
        dto.setDescripcion(tratamiento.getDescripcion());
        dto.setFechaInicio(tratamiento.getFechaInicio());
        dto.setFechaFin(tratamiento.getFechaFin());
        dto.setEstado(tratamiento.getEstado());
        return dto;
    }
}
