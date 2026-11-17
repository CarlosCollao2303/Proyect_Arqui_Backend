package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.CorreoException;
import pe.edu.upc.proyect_arqui_backend.repositories.INotificacionesRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICorreoService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacionesServiceImplement implements INotificacionesService {

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
