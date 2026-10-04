package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;
import pe.edu.upc.proyect_arqui_backend.dtos.CantidadDiagnosticosDTO;
import java.util.List;
import java.util.Optional;

public interface IHistoriasClinicasRepository extends JpaRepository<HistoriasClinicas, Integer> {
    @Query(value = "SELECT * FROM historiales_clinicos WHERE paciente_id = :idPaciente", nativeQuery = true)
    Optional<HistoriasClinicas> findByPacienteIdNative(@Param("idPaciente") int idPaciente);

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CantidadDiagnosticosDTO(" +
            "h.idHistoriaClinica, CONCAT(p.nombres, ' ', p.apellidos), COUNT(dg.idDiagnostico)) " +
            "FROM HistoriasClinicas h " +
            "JOIN h.paciente p " +
            "LEFT JOIN DetalleHistorial d ON d.historiaClinica = h " +
            "LEFT JOIN d.diagnostico dg " +
            "GROUP BY h.idHistoriaClinica, p.nombres, p.apellidos " +
            "ORDER BY COUNT(dg.idDiagnostico) DESC")
    List<CantidadDiagnosticosDTO> contarDiagnosticosPorHistoria();

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CantidadDiagnosticosDTO(" +
            "h.idHistoriaClinica, CONCAT(p.nombres, ' ', p.apellidos), COUNT(dg.idDiagnostico)) " +
            "FROM HistoriasClinicas h " +
            "JOIN h.paciente p " +
            "LEFT JOIN DetalleHistorial d ON d.historiaClinica = h " +
            "LEFT JOIN d.diagnostico dg " +
            "WHERE h.idHistoriaClinica = :idHistoria " +
            "GROUP BY h.idHistoriaClinica, p.nombres, p.apellidos")
    Optional<CantidadDiagnosticosDTO> contarDiagnosticosDeHistoria(@Param("idHistoria") int idHistoria);
}
