package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;

import java.util.List;

public interface INotificacionesRepository extends JpaRepository<Notificaciones, Integer> {
    List<Notificaciones> findByUsuario_IdUsuarioOrderByFechaEnvioDesc(int idUsuario);

    List<Notificaciones> findByUsuario_IdUsuarioAndEstadoEnvioIgnoreCaseOrderByFechaEnvioDesc(
            int idUsuario, String estadoEnvio);
}
