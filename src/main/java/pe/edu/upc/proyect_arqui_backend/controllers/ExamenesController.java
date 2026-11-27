package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.CantidadExamenesDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.ExamenPendienteDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.ExamenesDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.TiempoExamenDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IExamenesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/examenes")
public class ExamenesController {
    private final IExamenesService eS;
    private final IDetalleHistorialService dhS;

    public ExamenesController(IExamenesService eS, IDetalleHistorialService dhS) {
        this.eS = eS;
        this.dhS = dhS;
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/Listar")
    public ResponseEntity<List<ExamenesDTO>> listar() {
        List<ExamenesDTO> lista = eS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/{id}")
    public ResponseEntity<ExamenesDTO> listarPorId(@PathVariable int id) {
        Examenes examen = eS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un examen con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(examen));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/Buscar")
    public ResponseEntity<List<ExamenesDTO>> buscarPorTipo(@RequestParam String tipoExamen) {
        List<ExamenesDTO> lista = eS.listByTipoExamen(tipoExamen)
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/cantidad-por-paciente")
    public ResponseEntity<List<CantidadExamenesDTO>> cantidadPorPaciente() {
        return ResponseEntity.ok(eS.contarExamenesPorPaciente());
    }

    // Reporte filtrado por el nombre del paciente: es mas facil que buscar por id.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/cantidad-por-paciente/buscar")
    public ResponseEntity<List<CantidadExamenesDTO>> cantidadPorNombreDePaciente(@RequestParam String nombre) {
        if (nombre.isBlank()) {
            throw new BadRequestException("El nombre del paciente no puede estar vacio");
        }

        return ResponseEntity.ok(eS.contarExamenesPorNombreDePaciente(nombre.trim()));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/cantidad-por-paciente/{idPaciente}")
    public ResponseEntity<CantidadExamenesDTO> cantidadDePaciente(@PathVariable int idPaciente) {
        return ResponseEntity.ok(eS.contarExamenesDePaciente(idPaciente));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/pendientes")
    public ResponseEntity<List<ExamenPendienteDTO>> pendientes() {
        return ResponseEntity.ok(eS.listarExamenesPendientes());
    }

    // Reporte para el grafico combinado: examenes con resultado y pendientes (barras apiladas)
    // y dias promedio hasta el resultado (linea), por tipo de examen.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/tiempo-entrega")
    public ResponseEntity<List<TiempoExamenDTO>> tiempoEntregaPorTipo() {
        List<TiempoExamenDTO> lista = eS.obtenerTiempoEntregaPorTipo()
                .stream()
                .map(item -> {
                    TiempoExamenDTO dto = new TiempoExamenDTO();

                    dto.setTipoExamen((String) item[0]);
                    dto.setSolicitados(((Number) item[1]).longValue());
                    dto.setConResultado(((Number) item[2]).longValue());
                    dto.setPendientes(((Number) item[3]).longValue());
                    // Sin examenes con resultado los dias llegan null
                    dto.setDiasPromedio(item[4] != null ? ((Number) item[4]).doubleValue() : null);
                    dto.setDiasMaximo(item[5] != null ? ((Number) item[5]).doubleValue() : null);

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @PostMapping("/Registrar")
    public ResponseEntity<ExamenesDTO> registrar(@Valid @RequestBody ExamenesDTO dto) {
        validarFechas(dto);

        Examenes examen = new Examenes();
        examen.setTipoExamen(dto.getTipoExamen());
        examen.setDescripcion(dto.getDescripcion());
        examen.setFechaSolicitud(dto.getFechaSolicitud().atStartOfDay());
        examen.setFechaResultado(dto.getFechaResultado() == null
                ? null : dto.getFechaResultado().atStartOfDay());

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

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @PutMapping("/Actualizar")
    public ResponseEntity<ExamenesDTO> actualizar(@Valid @RequestBody ExamenesDTO dto) {
        validarFechas(dto);

        Optional<Examenes> existente = eS.listId(dto.getIdExamen());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un examen con el id: " + dto.getIdExamen()
            );
        }

        Examenes examen = existente.get();
        examen.setTipoExamen(dto.getTipoExamen());
        examen.setDescripcion(dto.getDescripcion());
        examen.setFechaSolicitud(dto.getFechaSolicitud().atStartOfDay());
        examen.setFechaResultado(dto.getFechaResultado() == null
                ? null : dto.getFechaResultado().atStartOfDay());

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

    // fechaResultado es opcional (null = examen pendiente); si viene, no puede ser
    // anterior a la solicitud.
    private void validarFechas(ExamenesDTO dto) {
        LocalDateTime fechaSolicitud = dto.getFechaSolicitud().atStartOfDay();
        LocalDateTime fechaResultado = dto.getFechaResultado() == null
                ? null : dto.getFechaResultado().atStartOfDay();
        if (fechaResultado != null && fechaResultado.isBefore(fechaSolicitud)) {
            throw new BadRequestException("La fecha de resultado no puede ser anterior a la fecha de solicitud");
        }
    }

    private ExamenesDTO convertirADTO(Examenes examen) {
        ExamenesDTO dto = new ExamenesDTO();
        dto.setIdExamen(examen.getIdExamen());
        dto.setTipoExamen(examen.getTipoExamen());
        dto.setDescripcion(examen.getDescripcion());
        dto.setFechaSolicitud(examen.getFechaSolicitud().toLocalDate());
        dto.setFechaResultado(examen.getFechaResultado() == null
                ? null : examen.getFechaResultado().toLocalDate());
        return dto;
    }
}
