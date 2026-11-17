package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaPacienteDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitasDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.VideollamadaDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.CorreoException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/citas")
public class CitasController {
    private static final Logger log = LoggerFactory.getLogger(CitasController.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ICitasService cS;
    private final IUsuariosService uS;
    private final INotificacionesService nS;

    // Servidor de Jitsi Meet donde se crean las salas (por defecto el publico).
    @Value("${jitsi.dominio:meet.jit.si}")
    private String dominioJitsi;

    public CitasController(ICitasService cS, IUsuariosService uS, INotificacionesService nS) {
        this.cS = cS;
        this.uS = uS;
        this.nS = nS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<CitasDTO>> listar() {
        List<CitasDTO> lista = cS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }
    @GetMapping("/paciente/mis-citas")
    public ResponseEntity<List<CitaPacienteDTO>> misCitasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication auth) {
        if (tieneRol(auth, "ADMIN")) {
            return ResponseEntity.ok(cS.listarTodasLasCitasPorFecha(fecha));
        }
        return ResponseEntity.ok(cS.listarCitasDelPacientePorFecha(correo(auth), fecha));
    }
    // Las citas del usuario logueado: como paciente o como medico. El correo sale
    // del token (subject del JWT), no del request, asi nadie puede pedir las de otro.
    @GetMapping("/mis-citas")
    public ResponseEntity<List<CitasDTO>> misCitas(Authentication auth) {
        List<CitasDTO> lista = cS.listByParticipanteCorreo(correo(auth))
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitasDTO> listarPorId(@PathVariable int id) {
        Citas cita = cS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una cita con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(cita));
    }

    @GetMapping("/{idCita}/estado")
    public ResponseEntity<CitaEstadoDTO> consultarEstado(@PathVariable int idCita, Authentication auth) {
        boolean puedeVerTodas = auth == null || tieneRol(auth, "ADMIN") || tieneRol(auth, "MEDICO");

        return ResponseEntity.ok(cS.consultarEstado(idCita, correo(auth), puedeVerTodas));
    }

    // Datos para entrar a la videollamada (Jitsi Meet) de la cita. Si hay token, solo
    // el paciente o el medico de la cita (o un ADMIN) pueden pedirlos.
    @GetMapping("/{id}/videollamada")
    public ResponseEntity<VideollamadaDTO> videollamada(@PathVariable int id, Authentication auth) {
        Citas cita = cS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una cita con el id: " + id
                        )
                );

        if (auth != null && !tieneRol(auth, "ADMIN")
                && !cita.getPaciente().getCorreo().equals(correo(auth))
                && !cita.getMedico().getCorreo().equals(correo(auth))) {
            throw new AccessDeniedException("Solo el paciente o el medico de la cita pueden ingresar a la videollamada");
        }

        if ("CANCELADA".equals(cita.getEstado())) {
            throw new BadRequestException("La cita fue cancelada, no tiene videollamada");
        }

        // Las citas registradas antes de esta funcionalidad no tienen sala: se crea al pedirla.
        if (cita.getSalaVideollamada() == null || cita.getSalaVideollamada().isBlank()) {
            cita.setSalaVideollamada(generarSalaVideollamada());
            cS.update(cita);
        }

        return ResponseEntity.ok(convertirAVideollamadaDTO(cita));
    }

    @GetMapping("/medico/mis-citas")
    public ResponseEntity<List<CitaMedicoDTO>> misCitasComoMedico(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Authentication auth) {
        return ResponseEntity.ok(cS.listarCitasDelMedico(correo(auth), estado, inicio, fin));
    }

    // Un PACIENTE solo reserva para si mismo: el paciente sale del token y se ignora
    // el idPaciente del body. Ademas la cita nace PENDIENTE y sin tiempo de espera;
    // esos campos los maneja el personal. ADMIN y MEDICO pueden registrar para cualquiera.
    @PostMapping("/Registrar")
    public ResponseEntity<CitasDTO> registrar(@Valid @RequestBody CitasDTO dto, Authentication auth) {
        // Solo al registrar: al actualizar, una cita que ya paso si puede tener fecha
        // anterior (por ejemplo, para marcarla ATENDIDA).
        if (dto.getFechaHoraProgramada().isBefore(LocalDate.now())) {
            throw new BadRequestException("No se puede registrar una cita en una fecha y hora pasada");
        }

        boolean esPaciente = auth != null && !tieneRol(auth, "ADMIN") && !tieneRol(auth, "MEDICO");

        Usuarios paciente;
        if (esPaciente) {
            paciente = uS.listByCorreo(correo(auth))
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "No existe el usuario con el correo: " + correo(auth)
                            )
                    );
        } else {
            paciente = obtenerPaciente(dto.getIdPaciente());
        }

        Usuarios medico = obtenerMedico(dto.getIdMedico());

        Citas cita = new Citas();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHoraProgramada(dto.getFechaHoraProgramada().atStartOfDay());
        cita.setTiempoEsperaMinutos(esPaciente ? 0 : dto.getTiempoEsperaMinutos());
        cita.setEstado(esPaciente || dto.getEstado() == null
                ? Citas.ESTADO_PENDIENTE
                : validarEstado(dto.getEstado()));
        cita.setSalaVideollamada(generarSalaVideollamada());

        cS.insert(cita);
        notificarCitaRegistrada(cita);

        CitasDTO responseDTO = convertirADTO(cita);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cita.getIdCita())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    public ResponseEntity<CitasDTO> actualizar(@Valid @RequestBody CitasDTO dto) {
        Optional<Citas> existente = cS.listId(dto.getIdCita());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una cita con el id: " + dto.getIdCita()
            );
        }

        Usuarios paciente = obtenerPaciente(dto.getIdPaciente());
        Usuarios medico = obtenerMedico(dto.getIdMedico());

        Citas cita = existente.get();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHoraProgramada(dto.getFechaHoraProgramada().atStartOfDay());
        cita.setTiempoEsperaMinutos(dto.getTiempoEsperaMinutos());
        cita.setEstado(validarEstado(dto.getEstado()));

        cS.update(cita);

        CitasDTO responseDTO = convertirADTO(cita);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Citas cita = cS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una cita con el id: " + id
                        )
                );

        cS.delete(cita.getIdCita());
        return ResponseEntity.noContent().build();
    }

    // Igual que con el medico: ademas de existir, el usuario tiene que tener rol
    // PACIENTE. Si no, se podria agendar una cita donde el "paciente" es un medico o un admin.
    private Usuarios obtenerPaciente(Integer idPaciente) {
        if (idPaciente == null) {
            throw new BadRequestException("El id del paciente es obligatorio");
        }

        Usuarios paciente = uS.listId(idPaciente)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el paciente con el id: " + idPaciente
                        )
                );

        if (!"PACIENTE".equals(paciente.getRol().getNombre())) {
            throw new BadRequestException("El usuario con id " + idPaciente + " no es paciente");
        }

        return paciente;
    }

    // Ademas de existir, el usuario tiene que tener rol MEDICO: si no, se podria
    // agendar una cita "con" un paciente o un admin.
    private Usuarios obtenerMedico(Integer idMedico) {
        Usuarios medico = uS.listId(idMedico)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el medico con el id: " + idMedico
                        )
                );

        if (!"MEDICO".equals(medico.getRol().getNombre())) {
            throw new BadRequestException("El usuario con id " + idMedico + " no es medico");
        }

        return medico;
    }

    private String validarEstado(String estado) {
        String normalizado = estado == null ? "" : estado.trim().toUpperCase();

        if (!Citas.ESTADOS.contains(normalizado)) {
            throw new BadRequestException("El estado de la cita debe ser uno de: " + Citas.ESTADOS);
        }

        return normalizado;
    }

    // Sala unica de Jitsi Meet: el UUID evita que se pueda adivinar la de otra cita.
    private String generarSalaVideollamada() {
        return "SaludCerca-" + UUID.randomUUID().toString().replace("-", "");
    }

    // Avisa al paciente por correo (Resend) con el enlace a la videollamada. La cita
    // ya quedo registrada, asi que si el correo falla no se revierte: la notificacion
    // queda guardada como FALLIDO y solo se deja constancia en el log.
    private void notificarCitaRegistrada(Citas cita) {
        Usuarios medico = cita.getMedico();
        String mensaje = "Tu cita con " + medico.getNombres() + " " + medico.getApellidos()
                + " quedo registrada para el "
                + cita.getFechaHoraProgramada().toLocalDate().format(FORMATO_FECHA) + ".\n"
                + "Ese dia puedes ingresar a la consulta por videollamada desde el siguiente boton.";

        try {
            nS.enviarPorCorreo(cita.getPaciente(), "CITA_REGISTRADA",
                    "SaludCerca: tu cita fue registrada", mensaje, enlaceVideollamada(cita));
        } catch (CorreoException e) {
            log.warn("No se pudo notificar la cita {} por correo: {}", cita.getIdCita(), e.getMessage());
        }
    }

    private String enlaceVideollamada(Citas cita) {
        return "https://" + dominioJitsi + "/" + cita.getSalaVideollamada();
    }

    private VideollamadaDTO convertirAVideollamadaDTO(Citas cita) {
        return new VideollamadaDTO(
                cita.getIdCita(),
                dominioJitsi,
                cita.getSalaVideollamada(),
                enlaceVideollamada(cita),
                cita.getPaciente().getNombres() + " " + cita.getPaciente().getApellidos(),
                cita.getMedico().getNombres() + " " + cita.getMedico().getApellidos(),
                cita.getFechaHoraProgramada().toLocalDate(),
                cita.getEstado()
        );
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth != null && auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }

    private String correo(Authentication auth) {
        return auth == null ? null : auth.getName();
    }

    private CitasDTO convertirADTO(Citas cita) {
        CitasDTO dto = new CitasDTO();
        dto.setIdCita(cita.getIdCita());
        dto.setIdPaciente(cita.getPaciente().getIdUsuario());
        dto.setIdMedico(cita.getMedico().getIdUsuario());
        dto.setFechaHoraProgramada(cita.getFechaHoraProgramada().toLocalDate());
        dto.setTiempoEsperaMinutos(cita.getTiempoEsperaMinutos());
        dto.setEstado(cita.getEstado());
        return dto;
    }

}
