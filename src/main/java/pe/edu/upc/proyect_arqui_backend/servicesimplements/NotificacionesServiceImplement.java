package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.repositories.INotificacionesRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificacionesServiceImplement implements INotificacionesService {

    private final INotificacionesRepository nR;
    public NotificacionesServiceImplement(INotificacionesRepository nR) {
        this.nR = nR;
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
}
