package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.dtos.CantidadExamenesDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.ExamenPendienteDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;

import java.util.List;
import java.util.Optional;

public interface IExamenesRepository extends JpaRepository<Examenes, Integer> {
    List<Examenes> findByTipoExamenContainingIgnoreCase(String tipoExamen);
    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CantidadExamenesDTO(" +
            "u.idUsuario, CONCAT(u.nombres, ' ', u.apellidos), u.dni, COUNT(e.idExamen)) " +
            "FROM Usuarios u " +
            "JOIN u.rol r " +
            "LEFT JOIN HistoriasClinicas h ON h.paciente = u " +
            "LEFT JOIN DetalleHistorial d ON d.historiaClinica = h " +
            "LEFT JOIN d.examen e " +
            "WHERE r.nombre = 'PACIENTE' " +
            "GROUP BY u.idUsuario, u.nombres, u.apellidos, u.dni " +
            "ORDER BY COUNT(e.idExamen) DESC")
    List<CantidadExamenesDTO> contarExamenesPorPaciente();

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CantidadExamenesDTO(" +
            "u.idUsuario, CONCAT(u.nombres, ' ', u.apellidos), u.dni, COUNT(e.idExamen)) " +
            "FROM Usuarios u " +
            "JOIN u.rol r " +
            "LEFT JOIN HistoriasClinicas h ON h.paciente = u " +
            "LEFT JOIN DetalleHistorial d ON d.historiaClinica = h " +
            "LEFT JOIN d.examen e " +
            "WHERE r.nombre = 'PACIENTE' AND u.idUsuario = :idPaciente " +
            "GROUP BY u.idUsuario, u.nombres, u.apellidos, u.dni")
    Optional<CantidadExamenesDTO> contarExamenesDePaciente(@Param("idPaciente") int idPaciente);

    // Examenes que aun no tienen resultado, del mas antiguo al mas reciente.
    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.ExamenPendienteDTO(" +
            "e.idExamen, e.tipoExamen, e.fechaSolicitud) " +
            "FROM Examenes e " +
            "WHERE e.fechaResultado IS NULL " +
            "ORDER BY e.fechaSolicitud ASC")
    List<ExamenPendienteDTO> listarExamenesPendientes();
}
