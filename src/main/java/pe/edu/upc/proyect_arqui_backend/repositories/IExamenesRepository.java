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

    // Para comparar sin tildes: TRANSLATE cambia cada vocal con tilde (y la enie) por la
    // letra sin tilde. Existe en PostgreSQL y en H2 (la base de los tests).
    String CON_TILDES = "\u00e1\u00e9\u00ed\u00f3\u00fa\u00fc\u00f1";
    String SIN_TILDES = "aeiouun";

    // Igual que contarExamenesPorPaciente, pero solo los pacientes cuyo nombre completo
    // contiene el texto buscado (sin importar mayusculas ni tildes).
    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CantidadExamenesDTO(" +
            "u.idUsuario, CONCAT(u.nombres, ' ', u.apellidos), u.dni, COUNT(e.idExamen)) " +
            "FROM Usuarios u " +
            "JOIN u.rol r " +
            "LEFT JOIN HistoriasClinicas h ON h.paciente = u " +
            "LEFT JOIN DetalleHistorial d ON d.historiaClinica = h " +
            "LEFT JOIN d.examen e " +
            "WHERE r.nombre = 'PACIENTE' " +
            "AND CAST(FUNCTION('TRANSLATE', LOWER(CONCAT(u.nombres, ' ', u.apellidos)), '" + CON_TILDES + "', '" + SIN_TILDES + "') AS String) " +
            "LIKE CONCAT('%', CAST(FUNCTION('TRANSLATE', LOWER(:nombre), '" + CON_TILDES + "', '" + SIN_TILDES + "') AS String), '%') " +
            "GROUP BY u.idUsuario, u.nombres, u.apellidos, u.dni " +
            "ORDER BY COUNT(e.idExamen) DESC")
    List<CantidadExamenesDTO> contarExamenesPorNombreDePaciente(@Param("nombre") String nombre);

    // Examenes que aun no tienen resultado, del mas antiguo al mas reciente.
    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.ExamenPendienteDTO(" +
            "e.idExamen, e.tipoExamen, e.fechaSolicitud) " +
            "FROM Examenes e " +
            "WHERE e.fechaResultado IS NULL " +
            "ORDER BY e.fechaSolicitud ASC")
    List<ExamenPendienteDTO> listarExamenesPendientes();

    // Dias entre la solicitud y el resultado. Se restan los EPOCH (segundos) de cada fecha
    // porque funciona igual en PostgreSQL y en H2; restar las fechas directo da un INTERVAL
    // distinto en cada base.
    String DIAS_ENTREGA = "(EXTRACT(EPOCH FROM e.fecha_resultado) - EXTRACT(EPOCH FROM e.fecha_solicitud)) / 86400";

    // Reporte: tiempo de entrega de cada tipo de examen. Los dias solo salen de los examenes
    // con resultado (en los pendientes la resta da NULL y AVG/MAX la ignoran); si ningun examen
    // del tipo tiene resultado, los dias salen NULL y ese tipo va al final.
    // Cada fila: [tipo de examen, solicitados, con resultado, pendientes, dias promedio, dias maximo]
    @Query(value = "SELECT e.tipo_examen AS tipo, " +
            " COUNT(*) AS solicitados, " +
            " SUM(CASE WHEN e.fecha_resultado IS NOT NULL THEN 1 ELSE 0 END) AS con_resultado, " +
            " SUM(CASE WHEN e.fecha_resultado IS NULL THEN 1 ELSE 0 END) AS pendientes, " +
            " ROUND(AVG(CAST(" + DIAS_ENTREGA + " AS DECIMAL(10, 2))), 1) AS dias_promedio, " +
            " ROUND(MAX(CAST(" + DIAS_ENTREGA + " AS DECIMAL(10, 2))), 1) AS dias_maximo " +
            " FROM examenes e " +
            " GROUP BY e.tipo_examen " +
            " ORDER BY dias_promedio DESC NULLS LAST, tipo ASC", nativeQuery = true)
    List<Object[]> obtenerTiempoEntregaPorTipo();
}
