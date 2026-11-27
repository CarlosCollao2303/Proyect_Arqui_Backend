package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upc.proyect_arqui_backend.dtos.DiagnosticoFrecuenteDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Diagnostico;

import java.util.List;

public interface IDiagnosticoRepository extends JpaRepository<Diagnostico, Integer> {
    List<Diagnostico> findByCodigoCie10IgnoreCase(String codigoCie10);

    List<Diagnostico> findByDescripcionContainingIgnoreCase(String descripcion);

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.DiagnosticoFrecuenteDTO(" +
            "d.codigoCie10, d.descripcion, COUNT(d)) " +
            "FROM Diagnostico d " +
            "GROUP BY d.codigoCie10, d.descripcion " +
            "ORDER BY COUNT(d) DESC, d.codigoCie10 ASC")
    List<DiagnosticoFrecuenteDTO> listarDiagnosticosFrecuentes();
}
