package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.dtos.MensajeCitaDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.CorreoException;
import pe.edu.upc.proyect_arqui_backend.repositories.INotificacionesRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICorreoService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacionesServiceImplement implements INotificacionesService {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String INDICACION_VIDEOLLAMADA =
            "Ese dia puedes ingresar a la consulta por videollamada desde el siguiente boton, "
                    + "desde tu computadora o tu celular.";

    private final INotificacionesRepository nR;
    private final ICorreoService correoService;
    public NotificacionesServiceImplement(INotificacionesRepository nR, ICorreoService correoService) {
        this.nR = nR;
        this.correoService = correoService;
    }

    @Override
    public Notificaciones enviarPorCorreo(Usuarios usuario, String tipo, String asunto,
                                          String mensaje, String enlaceVideollamada) {
        Notificaciones notificacion = new Notificaciones();
        notificacion.setUsuario(usuario);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje);
        notificacion.setFechaEnvio(LocalDateTime.now());

        try {
            correoService.enviar(usuario.getCorreo(), asunto,
                    armarHtml(usuario, mensaje, enlaceVideollamada));
            notificacion.setEstadoEnvio(Notificaciones.ESTADO_ENVIADO);
            nR.save(notificacion);
            return notificacion;
        } catch (CorreoException e) {
            // Se guarda igual para que quede el historial del intento fallido.
            notificacion.setEstadoEnvio(Notificaciones.ESTADO_FALLIDO);
            nR.save(notificacion);
            throw e;
        }
    }

    // Mensajes pre armados: cada uno se arma con los datos que ya tiene la cita
    // (paciente, medico, especialidad, fecha, estado y tiempo de espera).
    @Override
    public MensajeCitaDTO armarMensajeCita(Citas cita, String tipo) {
        String tipoFinal = tipo != null ? tipo : tipoSegunEstado(cita);

        Usuarios paciente = cita.getPaciente();
        Usuarios medico = cita.getMedico();
        String conMedico = "con " + nombreCompleto(medico)
                + (medico.getEspecialidad() != null ? " (" + medico.getEspecialidad().getNombre() + ")" : "");
        String fecha = cita.getFechaHoraProgramada().toLocalDate().format(FORMATO_FECHA);

        String asunto;
        String mensaje;
        switch (tipoFinal) {
            case Notificaciones.TIPO_CITA_REGISTRADA -> {
                asunto = "SaludCerca: tu cita fue registrada";
                mensaje = String.format("Tu cita #%d %s quedo registrada para el %s.",
                        cita.getIdCita(), conMedico, fecha)
                        + "\n" + INDICACION_VIDEOLLAMADA;
            }
            case Notificaciones.TIPO_RECORDATORIO -> {
                asunto = String.format("SaludCerca: recordatorio de tu cita del %s", fecha);
                mensaje = String.format("Te recordamos tu cita #%d %s programada para el %s.",
                        cita.getIdCita(), conMedico, fecha)
                        + "\n" + INDICACION_VIDEOLLAMADA;
            }
            case Notificaciones.TIPO_CITA_REPROGRAMADA -> {
                asunto = "SaludCerca: tu cita cambio de fecha";
                mensaje = String.format("Tu cita #%d %s fue reprogramada para el %s.",
                        cita.getIdCita(), conMedico, fecha)
                        + "\n" + INDICACION_VIDEOLLAMADA;
            }
            case Notificaciones.TIPO_CITA_ATENDIDA -> {
                asunto = "SaludCerca: gracias por asistir a tu cita";
                mensaje = String.format("Tu cita #%d %s del %s fue atendida.",
                        cita.getIdCita(), conMedico, fecha)
                        + (cita.getTiempoEsperaMinutos() > 0
                        ? String.format(" Tu tiempo de espera fue de %d minutos.", cita.getTiempoEsperaMinutos())
                        : "")
                        + "\nPuedes revisar tus examenes y recetas en Mi historia clinica de SaludCerca.";
            }
            case Notificaciones.TIPO_CITA_CANCELADA -> {
                asunto = "SaludCerca: tu cita fue cancelada";
                mensaje = String.format("Tu cita #%d %s del %s fue cancelada.",
                        cita.getIdCita(), conMedico, fecha)
                        + "\nSi necesitas una nueva cita, puedes reservarla en SaludCerca.";
            }
            default -> throw new BadRequestException("No hay un mensaje de cita para el tipo: " + tipoFinal);
        }

        return new MensajeCitaDTO(cita.getIdCita(), tipoFinal, asunto,
                nombreCompleto(paciente), paciente.getCorreo(), mensaje);
    }

    @Override
    public Notificaciones notificarCita(Citas cita, String tipo, String enlaceVideollamada) {
        MensajeCitaDTO mensaje = armarMensajeCita(cita, tipo);

        // Solo las citas que siguen pendientes llevan el boton para entrar a la videollamada.
        String enlace = Citas.ESTADO_PENDIENTE.equals(cita.getEstado()) ? enlaceVideollamada : null;

        return enviarPorCorreo(cita.getPaciente(), mensaje.tipo(), mensaje.asunto(),
                mensaje.mensaje(), enlace);
    }

    private String tipoSegunEstado(Citas cita) {
        return switch (cita.getEstado()) {
            case "ATENDIDA" -> Notificaciones.TIPO_CITA_ATENDIDA;
            case "CANCELADA" -> Notificaciones.TIPO_CITA_CANCELADA;
            default -> Notificaciones.TIPO_RECORDATORIO;
        };
    }

    private String nombreCompleto(Usuarios usuario) {
        return usuario.getNombres() + " " + usuario.getApellidos();
    }

    // El mensaje lo escribe un usuario, por eso se escapa antes de meterlo en el HTML.
    private String armarHtml(Usuarios usuario, String mensaje, String enlaceVideollamada) {
        String boton = "";
        if (enlaceVideollamada != null && !enlaceVideollamada.isBlank()) {
            boton = "<p style=\"margin:24px 0\"><a href=\"" + HtmlUtils.htmlEscape(enlaceVideollamada) + "\""
                    + " style=\"background:#176B87;color:#ffffff;padding:12px 20px;border-radius:8px;"
                    + "text-decoration:none;font-weight:bold;display:inline-block\">"
                    + "Ingresar a la videollamada</a></p>";
        }

        return "<div style=\"font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#243746\">"
                + "<h2 style=\"color:#176B87\">SaludCerca</h2>"
                + "<p>Hola " + HtmlUtils.htmlEscape(usuario.getNombres()) + ",</p>"
                + "<p>" + HtmlUtils.htmlEscape(mensaje).replace("\n", "<br>") + "</p>"
                + boton
                + "<p style=\"color:#526575;font-size:13px\">Este es un mensaje automatico de SaludCerca.</p>"
                + "</div>";
    }

    @Override
    public void insert(Notificaciones n) {
        nR.save(n);
    }

    @Override
    public void delete(int id) {
        nR.deleteById(id);
    }

    @Override
    public void update(Notificaciones n) {
        nR.save(n);
    }

    @Override
    public List<Notificaciones> list() {
        return nR.findAll();
    }

    @Override
    public Optional<Notificaciones> listId(int id) {
        return nR.findById(id);
    }

    @Override
    public List<Notificaciones> listByUsuarioId(int idUsuario) {
        return nR.findByUsuario_IdUsuarioOrderByFechaEnvioDesc(idUsuario);
    }

    @Override
    public List<Notificaciones> listByUsuarioIdAndEstadoEnvio(int idUsuario, String estadoEnvio) {
        return nR.findByUsuario_IdUsuarioAndEstadoEnvioIgnoreCaseOrderByFechaEnvioDesc(
                idUsuario, estadoEnvio);
    }
}
