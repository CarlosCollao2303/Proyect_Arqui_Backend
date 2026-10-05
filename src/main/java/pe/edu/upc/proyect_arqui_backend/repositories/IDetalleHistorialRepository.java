package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;

import java.util.List;

public interface IDetalleHistorialRepository extends JpaRepository<DetalleHistorial, Integer> {
    List<DetalleHistorial> findAllByExamen_IdExamen(int idExamen);

    List<DetalleHistorial> findAllByDiagnostico_IdDiagnostico(int idDiagnostico);

    List<DetalleHistorial> findAllByTratamiento_IdTratamiento(int idTratamiento);

    List<DetalleHistorial> findAllByReceta_IdReceta(int idReceta);
}
