package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.NotificacionesDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionesController {

    private final INotificacionesService nS;
    private final IUsuariosService uS;

    public NotificacionesController(INotificacionesService nS, IUsuariosService uS) {
        this.nS = nS;
        this.uS = uS;
    }

    @GetMapping
    public ResponseEntity<List<NotificacionesDTO>> listar() {
        List<NotificacionesDTO> lista = nS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<NotificacionesDTO> listarPorId(@PathVariable int id) {
        Notificaciones notificacion = nS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una notificacion con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(notificacion));
    }

    @PostMapping
    public ResponseEntity<NotificacionesDTO> registrar(@Valid @RequestBody NotificacionesDTO dto) {
        Usuarios usuario = uS.listId(dto.getIdUsuario())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el usuario con el id: " + dto.getIdUsuario()
                        )
                );

        Notificaciones notificacion = new Notificaciones();
        notificacion.setUsuario(usuario);
        notificacion.setTipo(dto.getTipo());
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setFechaEnvio(dto.getFechaEnvio());
        notificacion.setEstadoEnvio(dto.getEstadoEnvio());

        nS.insert(notificacion);

        NotificacionesDTO responseDTO = convertirADTO(notificacion);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(notificacion.getIdNotificacion())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping
    public ResponseEntity<NotificacionesDTO> actualizar(@Valid @RequestBody NotificacionesDTO dto) {
        Optional<Notificaciones> existente = nS.listId(dto.getIdNotificacion());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una notificacion con el id: " + dto.getIdNotificacion()
            );
        }

        Usuarios usuario = uS.listId(dto.getIdUsuario())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el usuario con el id: " + dto.getIdUsuario()
                        )
                );

        Notificaciones notificacion = existente.get();
        notificacion.setUsuario(usuario);
        notificacion.setTipo(dto.getTipo());
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setFechaEnvio(dto.getFechaEnvio());
        notificacion.setEstadoEnvio(dto.getEstadoEnvio());

        nS.update(notificacion);

        NotificacionesDTO responseDTO = convertirADTO(notificacion);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Notificaciones notificacion = nS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una notificacion con el id: " + id
                        )
                );

        nS.delete(notificacion.getIdNotificacion());
        return ResponseEntity.noContent().build();
    }

    private NotificacionesDTO convertirADTO(Notificaciones notificacion) {
        NotificacionesDTO dto = new NotificacionesDTO();
        dto.setIdNotificacion(notificacion.getIdNotificacion());
        dto.setIdUsuario(notificacion.getUsuario().getIdUsuario());
        dto.setTipo(notificacion.getTipo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setFechaEnvio(notificacion.getFechaEnvio());
        dto.setEstadoEnvio(notificacion.getEstadoEnvio());
        return dto;
    }
}
