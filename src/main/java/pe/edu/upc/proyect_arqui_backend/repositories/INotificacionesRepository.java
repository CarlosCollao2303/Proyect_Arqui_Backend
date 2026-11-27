package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upc.proyect_arqui_backend.dtos.ConteoNotificacionesEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;

import java.util.List;

public interface INotificacionesRepository extends JpaRepository<Notificaciones, Integer> {
    List<Notificaciones> findByUsuario_IdUsuarioOrderByFechaEnvioDesc(int idUsuario);

    List<Notificaciones> findByUsuario_IdUsuarioAndEstadoEnvioIgnoreCaseOrderByFechaEnvioDesc(
            int idUsuario, String estadoEnvio);

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.ConteoNotificacionesEstadoDTO(" +
            "n.estadoEnvio, COUNT(n)) " +
            "FROM Notificaciones n " +
            "GROUP BY n.estadoEnvio " +
            "ORDER BY COUNT(n) DESC, n.estadoEnvio ASC")
    List<ConteoNotificacionesEstadoDTO> contarNotificacionesPorEstado();
}
