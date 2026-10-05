package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.DetalleHistorialDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Diagnostico;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;
import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;
import pe.edu.upc.proyect_arqui_backend.entities.Recetas;
import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDiagnosticoService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IExamenesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IHistoriasClinicasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRecetasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ITratamientosService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
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
@RequestMapping("/detallehistorial")
public class DetalleHistorialController {

    private final IDetalleHistorialService dhS;
    private final IHistoriasClinicasService hS;
    private final IUsuariosService uS;
    private final IExamenesService eS;
    private final IDiagnosticoService dS;
    private final ITratamientosService tS;
    private final IRecetasService rS;

    public DetalleHistorialController(IDetalleHistorialService dhS, IHistoriasClinicasService hS,
                                      IUsuariosService uS, IExamenesService eS,
                                      IDiagnosticoService dS, ITratamientosService tS,
                                      IRecetasService rS) {
        this.dhS = dhS;
        this.hS = hS;
        this.uS = uS;
        this.eS = eS;
        this.dS = dS;
        this.tS = tS;
        this.rS = rS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<DetalleHistorialDTO>> listar() {
        List<DetalleHistorialDTO> lista = dhS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<DetalleHistorialDTO> listarPorId(@PathVariable int id) {
        DetalleHistorial detalle = dhS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un detalle de historial con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(detalle));
    }

    @GetMapping("/BuscarPorExamen/{idExamen}")
    public ResponseEntity<List<DetalleHistorialDTO>> buscarPorExamen(@PathVariable int idExamen) {
        return ResponseEntity.ok(dhS.findAllByExamenId(idExamen)
                .stream()
                .map(this::convertirADTO)
                .toList());
    }

    @GetMapping("/BuscarPorDiagnostico/{idDiagnostico}")
    public ResponseEntity<List<DetalleHistorialDTO>> buscarPorDiagnostico(@PathVariable int idDiagnostico) {
        return ResponseEntity.ok(dhS.findAllByDiagnosticoId(idDiagnostico)
                .stream()
                .map(this::convertirADTO)
                .toList());
    }

    @GetMapping("/BuscarPorTratamiento/{idTratamiento}")
    public ResponseEntity<List<DetalleHistorialDTO>> buscarPorTratamiento(@PathVariable int idTratamiento) {
        return ResponseEntity.ok(dhS.findAllByTratamientoId(idTratamiento)
                .stream()
                .map(this::convertirADTO)
                .toList());
    }

    @GetMapping("/BuscarPorReceta/{idReceta}")
    public ResponseEntity<List<DetalleHistorialDTO>> buscarPorReceta(@PathVariable int idReceta) {
        return ResponseEntity.ok(dhS.findAllByRecetaId(idReceta)
                .stream()
                .map(this::convertirADTO)
                .toList());
    }

    @PostMapping("/Registrar")
    @Transactional
    public ResponseEntity<DetalleHistorialDTO> registrar(@Valid @RequestBody DetalleHistorialDTO dto) {
        HistoriasClinicas historiaClinica = hS.listId(dto.getIdHistoriaClinica())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la historia clinica con el id: " + dto.getIdHistoriaClinica()
                        )
                );

        Usuarios medico = obtenerMedico(dto.getIdMedico());

        DetalleHistorial detalle = new DetalleHistorial();
        detalle.setHistoriaClinica(historiaClinica);
        detalle.setMedico(medico);
        detalle.setMotivoConsulta(dto.getMotivoConsulta());
        detalle.setCifradoDatos(dto.getCifradoDatos());
        detalle.setFechaRegistro(dto.getFechaRegistro());
        asignarRelaciones(detalle, dto);

        dhS.insert(detalle);

        DetalleHistorialDTO responseDTO = convertirADTO(detalle);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(detalle.getIdDetalleHistorial())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    @Transactional
    public ResponseEntity<DetalleHistorialDTO> actualizar(@Valid @RequestBody DetalleHistorialDTO dto) {
        Optional<DetalleHistorial> existente = dhS.listId(dto.getIdDetalleHistorial());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un detalle de historial con el id: " + dto.getIdDetalleHistorial()
            );
        }

        HistoriasClinicas historiaClinica = hS.listId(dto.getIdHistoriaClinica())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la historia clinica con el id: " + dto.getIdHistoriaClinica()
                        )
                );

        Usuarios medico = obtenerMedico(dto.getIdMedico());

        DetalleHistorial detalle = existente.get();
        detalle.setHistoriaClinica(historiaClinica);
        detalle.setMedico(medico);
        detalle.setMotivoConsulta(dto.getMotivoConsulta());
        detalle.setCifradoDatos(dto.getCifradoDatos());
        detalle.setFechaRegistro(dto.getFechaRegistro());
        asignarRelaciones(detalle, dto);

        dhS.update(detalle);

        DetalleHistorialDTO responseDTO = convertirADTO(detalle);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        DetalleHistorial detalle = dhS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un detalle de historial con el id: " + id
                        )
                );

        dhS.delete(detalle.getIdDetalleHistorial());
        return ResponseEntity.noContent().build();
    }

    private DetalleHistorialDTO convertirADTO(DetalleHistorial detalle) {
        DetalleHistorialDTO dto = new DetalleHistorialDTO();
        dto.setIdDetalleHistorial(detalle.getIdDetalleHistorial());
        dto.setIdHistoriaClinica(detalle.getHistoriaClinica().getIdHistoriaClinica());
        dto.setIdMedico(detalle.getMedico().getIdUsuario());
        dto.setIdExamen(detalle.getExamen() == null ? null : detalle.getExamen().getIdExamen());
        dto.setIdDiagnostico(detalle.getDiagnostico() == null
                ? null : detalle.getDiagnostico().getIdDiagnostico());
        dto.setIdTratamiento(detalle.getTratamiento() == null
                ? null : detalle.getTratamiento().getIdTratamiento());
        dto.setIdReceta(detalle.getReceta() == null ? null : detalle.getReceta().getIdReceta());
        dto.setMotivoConsulta(detalle.getMotivoConsulta());
        dto.setCifradoDatos(detalle.getCifradoDatos());
        dto.setFechaRegistro(detalle.getFechaRegistro());
        return dto;
    }

    private void asignarRelaciones(DetalleHistorial detalle, DetalleHistorialDTO dto) {
        detalle.setExamen(dto.getIdExamen() == null ? null : eS.listId(dto.getIdExamen())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el examen con el id: " + dto.getIdExamen())));
        detalle.setDiagnostico(dto.getIdDiagnostico() == null ? null : dS.listId(dto.getIdDiagnostico())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el diagnostico con el id: " + dto.getIdDiagnostico())));
        detalle.setTratamiento(dto.getIdTratamiento() == null ? null : tS.listId(dto.getIdTratamiento())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el tratamiento con el id: " + dto.getIdTratamiento())));
        detalle.setReceta(dto.getIdReceta() == null ? null : rS.listId(dto.getIdReceta())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la receta con el id: " + dto.getIdReceta())));
    }

    private Usuarios obtenerMedico(Integer idMedico) {
        Usuarios medico = uS.listId(idMedico)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el usuario con el id: " + idMedico
                        )
                );

        if (medico.getRol() == null || !"MEDICO".equalsIgnoreCase(medico.getRol().getNombre())) {
            throw new BadRequestException(
                    "El usuario con id " + idMedico + " no tiene el rol MEDICO; solo los medicos pueden registrar detalles de historia clinica"
            );
        }

        return medico;
    }
}
