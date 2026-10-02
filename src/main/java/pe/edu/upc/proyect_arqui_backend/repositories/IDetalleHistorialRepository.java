package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;

import java.util.Optional;

public interface IDetalleHistorialRepository extends JpaRepository<DetalleHistorial, Integer> {

    Optional<DetalleHistorial> findByExamen_IdExamen(int idExamen);

    Optional<DetalleHistorial> findByDiagnostico_IdDiagnostico(int idDiagnostico);

    Optional<DetalleHistorial> findByTratamiento_IdTratamiento(int idTratamiento);

    Optional<DetalleHistorial> findByReceta_IdReceta(int idReceta);
}
