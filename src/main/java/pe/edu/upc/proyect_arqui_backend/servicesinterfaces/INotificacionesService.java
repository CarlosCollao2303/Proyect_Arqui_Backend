package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;

import java.util.List;
import java.util.Optional;

public interface INotificacionesService {
    public void insert(Notificaciones n);
    public void delete(int id);
    public void update(Notificaciones n);
    public List<Notificaciones> list();
    public Optional<Notificaciones> listId(int id);
}
