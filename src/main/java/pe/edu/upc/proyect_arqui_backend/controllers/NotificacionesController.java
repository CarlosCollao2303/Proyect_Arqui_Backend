package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.MensajeCitaDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.NotificacionCitaDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.NotificacionesDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionesController {
    private final INotificacionesService nS;
    private final IUsuariosService uS;
    private final ICitasService cS;

    // Servidor de Jitsi Meet de las videollamadas (el mismo que usa CitasController).
    @Value("${jitsi.dominio:meet.jit.si}")
    private String dominioJitsi;

    public NotificacionesController(INotificacionesService nS, IUsuariosService uS, ICitasService cS) {
        this.nS = nS;
        this.uS = uS;
        this.cS = cS;
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping
    public ResponseEntity<List<NotificacionesDTO>> listar() {
        List<NotificacionesDTO> lista = nS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
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

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/PorUsuario/{idUsuario}")
    public ResponseEntity<List<NotificacionesDTO>> listarPorUsuario(@PathVariable int idUsuario) {
        List<NotificacionesDTO> lista = nS.listByUsuarioId(idUsuario)
                .stream()
                .map(this::convertirADTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/PorUsuario/{idUsuario}/Estado")
    public ResponseEntity<List<NotificacionesDTO>> listarPorUsuarioYEstado(
            @PathVariable int idUsuario,
            @RequestParam String estado) {
        if (estado.isBlank()) {
            throw new BadRequestException("El estado de envio no puede estar vacio");
        }

        List<NotificacionesDTO> lista = nS.listByUsuarioIdAndEstadoEnvio(idUsuario, estado.trim())
                .stream()
                .map(this::convertirADTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
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
        notificacion.setFechaEnvio(dto.getFechaEnvio() == null
                ? null : dto.getFechaEnvio().atStartOfDay());
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

    // Envia la notificacion al correo del usuario con Resend. La fecha y el estado
    // de envio los pone el servidor; si Resend falla se responde 502 y la
    // notificacion queda guardada como FALLIDO.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @PostMapping("/EnviarCorreo")
    public ResponseEntity<NotificacionesDTO> enviarCorreo(@Valid @RequestBody NotificacionesDTO dto) {
        Usuarios usuario = uS.listId(dto.getIdUsuario())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el usuario con el id: " + dto.getIdUsuario()
                        )
                );

        String tipo = dto.getTipo().trim().toUpperCase();
        String asunto = "SaludCerca: " + tipo.replace('_', ' ').toLowerCase();

        Notificaciones notificacion = nS.enviarPorCorreo(
                usuario, tipo, asunto, dto.getMensaje().trim(), null);

        NotificacionesDTO responseDTO = convertirADTO(notificacion);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/notificaciones/ListarPorId/{id}")
                .buildAndExpand(notificacion.getIdNotificacion())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    // Muestra el mensaje que se enviaria para la cita, sin enviarlo. El texto lo arma
    // el back con los datos de la cita: nadie tiene que escribirlo.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/VistaPrevia/{idCita}")
    public ResponseEntity<MensajeCitaDTO> vistaPrevia(@PathVariable int idCita, Authentication auth) {
        Citas cita = obtenerCita(idCita, auth);

        return ResponseEntity.ok(nS.armarMensajeCita(cita, null));
    }

    // Envia al paciente el mensaje pre armado de la cita segun su estado: recordatorio
    // (con el enlace de la videollamada), cita atendida o cita cancelada.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @PostMapping("/EnviarPorCita")
    public ResponseEntity<NotificacionesDTO> enviarPorCita(@Valid @RequestBody NotificacionCitaDTO dto,
                                                           Authentication auth) {
        Citas cita = obtenerCita(dto.getIdCita(), auth);

        Notificaciones notificacion = nS.notificarCita(cita, null, enlaceVideollamada(cita));

        NotificacionesDTO responseDTO = convertirADTO(notificacion);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/notificaciones/ListarPorId/{id}")
                .buildAndExpand(notificacion.getIdNotificacion())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
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
        notificacion.setFechaEnvio(dto.getFechaEnvio() == null
                ? null : dto.getFechaEnvio().atStartOfDay());
        notificacion.setEstadoEnvio(dto.getEstadoEnvio());

        nS.update(notificacion);

        NotificacionesDTO responseDTO = convertirADTO(notificacion);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
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

    // Un MEDICO solo puede notificar las citas que atiende; el ADMIN, cualquiera.
    private Citas obtenerCita(int idCita, Authentication auth) {
        Citas cita = cS.listId(idCita)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una cita con el id: " + idCita
                        )
                );

        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        if (!esAdmin && !cita.getMedico().getCorreo().equals(auth.getName())) {
            throw new AccessDeniedException("Solo puedes notificar las citas que atiendes");
        }

        return cita;
    }

    private String enlaceVideollamada(Citas cita) {
        if (cita.getSalaVideollamada() == null || cita.getSalaVideollamada().isBlank()) {
            return null;
        }
        return "https://" + dominioJitsi + "/" + cita.getSalaVideollamada();
    }

    private NotificacionesDTO convertirADTO(Notificaciones notificacion) {
        NotificacionesDTO dto = new NotificacionesDTO();
        dto.setIdNotificacion(notificacion.getIdNotificacion());
        dto.setIdUsuario(notificacion.getUsuario().getIdUsuario());
        dto.setTipo(notificacion.getTipo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setFechaEnvio(notificacion.getFechaEnvio() == null
                ? null : notificacion.getFechaEnvio().toLocalDate());
        dto.setEstadoEnvio(notificacion.getEstadoEnvio());
        return dto;
    }

}
