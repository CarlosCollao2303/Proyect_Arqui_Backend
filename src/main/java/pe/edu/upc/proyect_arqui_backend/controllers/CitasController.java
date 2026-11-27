package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaPacienteDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitasDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitasEspecialidadDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitasMesDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.EsperaMedicoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.VideollamadaDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.CorreoException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/citas")
public class CitasController {
    private static final Logger log = LoggerFactory.getLogger(CitasController.class);

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

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/Listar")
    public ResponseEntity<List<CitasDTO>> listar() {
        List<CitasDTO> lista = cS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PACIENTE')")
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
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO') or hasAuthority('PACIENTE')")
    @GetMapping("/mis-citas")
    public ResponseEntity<List<CitasDTO>> misCitas(Authentication auth) {
        List<CitasDTO> lista = cS.listByParticipanteCorreo(correo(auth))
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
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

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO') or hasAuthority('PACIENTE')")
    @GetMapping("/{idCita}/estado")
    public ResponseEntity<CitaEstadoDTO> consultarEstado(@PathVariable int idCita, Authentication auth) {
        boolean puedeVerTodas = auth == null || tieneRol(auth, "ADMIN") || tieneRol(auth, "MEDICO");

        return ResponseEntity.ok(cS.consultarEstado(idCita, correo(auth), puedeVerTodas));
    }

    // Las citas del paciente logueado con su estado. No pide ningun id: el paciente
    // sale del token, asi que solo puede ver las suyas.
    @PreAuthorize("hasAuthority('PACIENTE')")
    @GetMapping("/estados/mias")
    public ResponseEntity<List<CitaEstadoDTO>> misEstados(Authentication auth) {
        return ResponseEntity.ok(cS.listarEstadosDelPaciente(correo(auth)));
    }

    // Las citas de un paciente elegido por el personal (desde un desplegable por nombre).
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/estados/paciente/{idPaciente}")
    public ResponseEntity<List<CitaEstadoDTO>> estadosDePaciente(@PathVariable int idPaciente) {
        obtenerPaciente(idPaciente);

        return ResponseEntity.ok(cS.listarEstadosDelPacientePorId(idPaciente));
    }

    // Citas con los nombres ya resueltos. El ADMIN ve todas; el MEDICO solo las suyas.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/estados")
    public ResponseEntity<List<CitaEstadoDTO>> estados(Authentication auth) {
        String correoMedico = tieneRol(auth, "ADMIN") ? null : correo(auth);

        return ResponseEntity.ok(cS.listarEstadosDeCitas(correoMedico));
    }

    // Datos para entrar a la videollamada (Jitsi Meet) de la cita. Si hay token, solo
    // el paciente o el medico de la cita (o un ADMIN) pueden pedirlos.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO') or hasAuthority('PACIENTE')")
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

    // Reporte para el grafico de barras apiladas: citas de cada especialidad por estado.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/estado-por-especialidad")
    public ResponseEntity<List<CitasEspecialidadDTO>> citasPorEspecialidad() {
        List<CitasEspecialidadDTO> lista = cS.contarCitasPorEspecialidadYEstado()
                .stream()
                .map(item -> {
                    CitasEspecialidadDTO dto = new CitasEspecialidadDTO();

                    dto.setEspecialidad((String) item[0]);
                    dto.setAtendidas(((Number) item[1]).longValue());
                    dto.setPendientes(((Number) item[2]).longValue());
                    dto.setCanceladas(((Number) item[3]).longValue());
                    dto.setTotal(((Number) item[4]).longValue());

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Reporte para el grafico combinado: citas atendidas (barras) y espera promedio (linea).
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/espera-por-medico")
    public ResponseEntity<List<EsperaMedicoDTO>> esperaPorMedico() {
        List<EsperaMedicoDTO> lista = cS.obtenerEsperaPromedioPorMedico()
                .stream()
                .map(item -> {
                    EsperaMedicoDTO dto = new EsperaMedicoDTO();

                    dto.setMedico((String) item[0]);
                    dto.setEspecialidad((String) item[1]);
                    dto.setCitasAtendidas(((Number) item[2]).longValue());
                    dto.setEsperaPromedio(((Number) item[3]).doubleValue());
                    dto.setEsperaMaxima(((Number) item[4]).intValue());

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Reporte para el grafico combinado: citas de cada mes por estado (barras apiladas) y
    // porcentaje de cancelacion (linea). Siempre devuelve los 12 meses, los vacios en cero.
    // Sin el parametro anio usa el año actual.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/evolucion-mensual")
    public ResponseEntity<List<CitasMesDTO>> evolucionMensual(@RequestParam(required = false) Integer anio) {
        int anioReporte = anio != null ? anio : LocalDate.now().getYear();
        if (anioReporte < 2000 || anioReporte > 2100) {
            throw new BadRequestException("El año debe estar entre 2000 y 2100");
        }

        List<CitasMesDTO> meses = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            meses.add(new CitasMesDTO(mes));
        }

        for (Object[] item : cS.contarCitasPorMesYEstado(anioReporte)) {
            CitasMesDTO dto = meses.get(((Number) item[0]).intValue() - 1);

            dto.setAtendidas(((Number) item[1]).longValue());
            dto.setPendientes(((Number) item[2]).longValue());
            dto.setCanceladas(((Number) item[3]).longValue());
            dto.setTotal(((Number) item[4]).longValue());
        }

        return ResponseEntity.ok(meses);
    }

    // Años con citas, para elegir cual ver en el reporte de evolucion mensual.
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @GetMapping("/anios-con-citas")
    public ResponseEntity<List<Integer>> aniosConCitas() {
        return ResponseEntity.ok(cS.listarAniosConCitas());
    }

    @PreAuthorize("hasAuthority('MEDICO')")
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
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO') or hasAuthority('PACIENTE')")
    @PostMapping("/Registrar")
    public ResponseEntity<CitasDTO> registrar(@Valid @RequestBody CitasDTO dto, Authentication auth) {
        // Solo al registrar: al actualizar, una cita que ya paso si puede tener fecha
        // anterior (por ejemplo, para marcarla ATENDIDA).
        if (dto.getFechaHoraProgramada().isBefore(LocalDate.now())) {
            throw new BadRequestException("No se puede registrar una cita en una fecha y hora pasada");
        }

        boolean esPaciente = auth != null && !tieneRol(auth, "ADMIN") && !tieneRol(auth, "MEDICO");

        Usuarios paciente = esPaciente
                ? usuarioLogueado(auth)
                : obtenerPaciente(dto.getIdPaciente());

        // Un MEDICO registra las citas a su nombre: queda guardado quien la registro
        // y no tiene que buscarse a si mismo en una lista.
        Usuarios medico = esMedico(auth)
                ? usuarioLogueado(auth)
                : obtenerMedico(dto.getIdMedico());

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

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MEDICO')")
    @PutMapping("/Actualizar")
    public ResponseEntity<CitasDTO> actualizar(@Valid @RequestBody CitasDTO dto, Authentication auth) {
        Optional<Citas> existente = cS.listId(dto.getIdCita());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una cita con el id: " + dto.getIdCita()
            );
        }

        Usuarios paciente = obtenerPaciente(dto.getIdPaciente());
        // Si edita un MEDICO, la cita conserva su medico: solo el ADMIN puede reasignarla.
        Usuarios medico = esMedico(auth)
                ? existente.get().getMedico()
                : obtenerMedico(dto.getIdMedico());

        Citas cita = existente.get();
        // Como estaba antes del cambio, para saber si hay que avisarle al paciente.
        String estadoAnterior = cita.getEstado();
        LocalDate fechaAnterior = cita.getFechaHoraProgramada().toLocalDate();

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHoraProgramada(dto.getFechaHoraProgramada().atStartOfDay());
        cita.setTiempoEsperaMinutos(dto.getTiempoEsperaMinutos());
        cita.setEstado(validarEstado(dto.getEstado()));

        cS.update(cita);
        notificarCambios(cita, estadoAnterior, fechaAnterior);

        CitasDTO responseDTO = convertirADTO(cita);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
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
        if (idMedico == null) {
            throw new BadRequestException("El id del medico es obligatorio");
        }

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

    // Avisa al paciente por correo (Resend) con el enlace a la videollamada. El mensaje
    // es pre armado (NotificacionesServiceImplement.armarMensajeCita). La cita ya quedo
    // registrada, asi que si el correo falla no se revierte: la notificacion queda
    // guardada como FALLIDO y solo se deja constancia en el log.
    private void notificarCitaRegistrada(Citas cita) {
        notificar(cita, Notificaciones.TIPO_CITA_REGISTRADA);
    }

    // Avisa solo, sin que nadie escriba el mensaje, cuando la cita cambia: se atendio,
    // se cancelo, volvio a estar pendiente o (si sigue pendiente) se movio de fecha.
    private void notificarCambios(Citas cita, String estadoAnterior, LocalDate fechaAnterior) {
        String tipo = null;

        if (!cita.getEstado().equals(estadoAnterior)) {
            tipo = switch (cita.getEstado()) {
                case "ATENDIDA" -> Notificaciones.TIPO_CITA_ATENDIDA;
                case "CANCELADA" -> Notificaciones.TIPO_CITA_CANCELADA;
                default -> Notificaciones.TIPO_RECORDATORIO;
            };
        } else if (Citas.ESTADO_PENDIENTE.equals(cita.getEstado())
                && !cita.getFechaHoraProgramada().toLocalDate().equals(fechaAnterior)) {
            tipo = Notificaciones.TIPO_CITA_REPROGRAMADA;
        }

        if (tipo != null) {
            notificar(cita, tipo);
        }
    }

    private void notificar(Citas cita, String tipo) {
        try {
            nS.notificarCita(cita, tipo, enlaceVideollamada(cita));
        } catch (CorreoException e) {
            log.warn("No se pudo notificar la cita {} por correo: {}", cita.getIdCita(), e.getMessage());
        }
    }

    // null si la cita todavia no tiene sala (citas registradas antes de la videollamada).
    private String enlaceVideollamada(Citas cita) {
        if (cita.getSalaVideollamada() == null || cita.getSalaVideollamada().isBlank()) {
            return null;
        }
        return "https://" + dominioJitsi + "/" + cita.getSalaVideollamada();
    }

    private VideollamadaDTO convertirAVideollamadaDTO(Citas cita) {
        String enlace = enlaceVideollamada(cita);

        return new VideollamadaDTO(
                cita.getIdCita(),
                dominioJitsi,
                cita.getSalaVideollamada(),
                enlace,
                cita.getPaciente().getNombres() + " " + cita.getPaciente().getApellidos(),
                cita.getMedico().getNombres() + " " + cita.getMedico().getApellidos(),
                cita.getFechaHoraProgramada().toLocalDate(),
                cita.getEstado(),
                generarQr(enlace)
        );
    }

    // La sala de Jitsi es solo una URL: cualquier dispositivo que la abra entra a la misma
    // videollamada (en el celular se abre en el navegador o en la app Jitsi Meet). El QR
    // es esa URL como imagen PNG en base64, lista para un <img> en el front.
    private String generarQr(String enlace) {
        if (enlace == null) {
            return null;
        }

        try {
            BitMatrix matriz = new QRCodeWriter().encode(enlace, BarcodeFormat.QR_CODE, 240, 240,
                    Map.of(EncodeHintType.MARGIN, 1));

            BufferedImage imagen = new BufferedImage(matriz.getWidth(), matriz.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < matriz.getWidth(); x++) {
                for (int y = 0; y < matriz.getHeight(); y++) {
                    imagen.setRGB(x, y, matriz.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            ByteArrayOutputStream png = new ByteArrayOutputStream();
            ImageIO.write(imagen, "png", png);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(png.toByteArray());
        } catch (WriterException | IOException e) {
            // Sin QR igual se puede entrar con el enlace, asi que no se corta la respuesta.
            log.warn("No se pudo generar el QR de la videollamada {}: {}", enlace, e.getMessage());
            return null;
        }
    }

    // El usuario dueno del token (el subject del JWT es su correo).
    private Usuarios usuarioLogueado(Authentication auth) {
        return uS.listByCorreo(correo(auth))
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el usuario con el correo: " + correo(auth)
                        )
                );
    }

    // MEDICO sin ser ADMIN: lo que registra queda a su nombre.
    private boolean esMedico(Authentication auth) {
        return tieneRol(auth, "MEDICO") && !tieneRol(auth, "ADMIN");
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
