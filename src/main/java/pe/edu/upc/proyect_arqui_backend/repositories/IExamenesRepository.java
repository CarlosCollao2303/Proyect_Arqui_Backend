package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;

import java.util.List;

public interface IExamenesRepository extends JpaRepository<Examenes, Integer> {
    List<Examenes> findByTipoExamenContainingIgnoreCase(String tipoExamen);
}
