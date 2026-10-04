package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.Diagnostico;

import java.util.List;

public interface IDiagnosticoRepository extends JpaRepository<Diagnostico, Integer> {
    List<Diagnostico> findByCodigoCie10IgnoreCase(String codigoCie10);

    List<Diagnostico> findByDescripcionContainingIgnoreCase(String descripcion);
}
