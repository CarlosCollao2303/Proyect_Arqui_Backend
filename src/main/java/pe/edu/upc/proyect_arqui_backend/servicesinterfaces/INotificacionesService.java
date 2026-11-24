package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.dtos.MensajeCitaDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.util.List;
import java.util.Optional;

public interface INotificacionesService {
    public void insert(Notificaciones n);
    public void delete(int id);
    public void update(Notificaciones n);
    public List<Notificaciones> list();
    public Optional<Notificaciones> listId(int id);
    public List<Notificaciones> listByUsuarioId(int idUsuario);
    public List<Notificaciones> listByUsuarioIdAndEstadoEnvio(int idUsuario, String estadoEnvio);
    // Envia la notificacion al correo del usuario y la guarda con estado ENVIADO.
    // Si el envio falla la guarda como FALLIDO y vuelve a lanzar CorreoException.
    // enlaceVideollamada es opcional (null): si viene, el correo muestra un boton.
    public Notificaciones enviarPorCorreo(Usuarios usuario, String tipo, String asunto,
                                          String mensaje, String enlaceVideollamada);
    // Arma el asunto y el mensaje de una cita con sus datos. Si tipo es null, el tipo
    // sale del estado de la cita (PENDIENTE: recordatorio, ATENDIDA, CANCELADA).
    public MensajeCitaDTO armarMensajeCita(Citas cita, String tipo);
    // Arma el mensaje de la cita y se lo envia por correo a su paciente.
    public Notificaciones notificarCita(Citas cita, String tipo, String enlaceVideollamada);
}
