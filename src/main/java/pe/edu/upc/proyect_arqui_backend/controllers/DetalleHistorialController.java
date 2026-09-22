package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.DetalleHistorialDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IHistoriasClinicasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/detallehistorial")
public class DetalleHistorialController {

    private final IDetalleHistorialService dhS;
    private final IHistoriasClinicasService hS;
    private final IUsuariosService uS;

    public DetalleHistorialController(IDetalleHistorialService dhS, IHistoriasClinicasService hS, IUsuariosService uS) {
        this.dhS = dhS;
        this.hS = hS;
        this.uS = uS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<DetalleHistorialDTO>> listar() {
        List<DetalleHistorialDTO> lista = dhS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PostMapping("/Registrar")
    public ResponseEntity<DetalleHistorialDTO> registrar(@Valid @RequestBody DetalleHistorialDTO dto) {
        HistoriasClinicas historiaClinica = hS.listId(dto.getIdHistoriaClinica())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la historia clinica con el id: " + dto.getIdHistoriaClinica()
                        )
                );

        Usuarios medico = uS.listId(dto.getIdMedico())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el medico con el id: " + dto.getIdMedico()
                        )
                );

        DetalleHistorial detalle = new DetalleHistorial();
        detalle.setHistoriaClinica(historiaClinica);
        detalle.setMedico(medico);
        detalle.setMotivoConsulta(dto.getMotivoConsulta());
        detalle.setCifradoDatos(dto.getCifradoDatos());
        detalle.setFechaRegistro(dto.getFechaRegistro());

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

        Usuarios medico = uS.listId(dto.getIdMedico())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el medico con el id: " + dto.getIdMedico()
                        )
                );

        DetalleHistorial detalle = existente.get();
        detalle.setHistoriaClinica(historiaClinica);
        detalle.setMedico(medico);
        detalle.setMotivoConsulta(dto.getMotivoConsulta());
        detalle.setCifradoDatos(dto.getCifradoDatos());
        detalle.setFechaRegistro(dto.getFechaRegistro());

        dhS.update(detalle);

        DetalleHistorialDTO responseDTO = convertirADTO(detalle);

        return ResponseEntity.ok(responseDTO);
    }

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
        dto.setMotivoConsulta(detalle.getMotivoConsulta());
        dto.setCifradoDatos(detalle.getCifradoDatos());
        dto.setFechaRegistro(detalle.getFechaRegistro());
        return dto;
    }
}
