package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaPacienteDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;

import java.time.LocalDateTime;
import java.util.List;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ICitasRepository extends JpaRepository<Citas, Integer> {
    // Citas donde el usuario es el paciente o el medico, de la mas proxima a la mas lejana.
    @Query("SELECT c FROM Citas c " +
            "WHERE c.paciente.correo = :correo OR c.medico.correo = :correo " +
            "ORDER BY c.fechaHoraProgramada ASC")
    List<Citas> findByParticipanteCorreo(@Param("correo") String correo);

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO(" +
            "c.idCita, c.fechaHoraProgramada, c.estado, " +
            "CONCAT(p.nombres, ' ', p.apellidos), " +
            "CONCAT(m.nombres, ' ', m.apellidos), " +
            "e.nombre) " +
            "FROM Citas c " +
            "JOIN c.paciente p " +
            "JOIN c.medico m " +
            "LEFT JOIN m.especialidad e " +
            "WHERE c.idCita = :idCita")
    Optional<CitaEstadoDTO> findEstadoById(@Param("idCita") int idCita);

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CitaPacienteDTO(" +
            "c.idCita, c.fechaHoraProgramada, c.estado, " +
            "CONCAT(m.nombres, ' ', m.apellidos), " +
            "e.nombre) " +
            "FROM Citas c " +
            "JOIN c.paciente p " +
            "JOIN c.medico m " +
            "LEFT JOIN m.especialidad e " +
            "WHERE p.correo = :correo " +
            "AND c.fechaHoraProgramada >= :inicio " +
            "AND c.fechaHoraProgramada < :fin " +
            "ORDER BY c.fechaHoraProgramada ASC")
    List<CitaPacienteDTO> findCitasDelPacienteEntre(@Param("correo") String correo,
                                                    @Param("inicio") LocalDateTime inicio,
                                                    @Param("fin") LocalDateTime fin);

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CitaPacienteDTO(" +
            "c.idCita, c.fechaHoraProgramada, c.estado, " +
            "CONCAT(p.nombres, ' ', p.apellidos), " +
            "CONCAT(m.nombres, ' ', m.apellidos), " +
            "e.nombre) " +
            "FROM Citas c " +
            "JOIN c.paciente p " +
            "JOIN c.medico m " +
            "LEFT JOIN m.especialidad e " +
            "WHERE c.fechaHoraProgramada >= :inicio " +
            "AND c.fechaHoraProgramada < :fin " +
            "ORDER BY c.fechaHoraProgramada ASC")
    List<CitaPacienteDTO> findTodasLasCitasEntre(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fin") LocalDateTime fin);

    boolean existsByIdCitaAndPacienteCorreo(int idCita, String correo);

    // Cada filtro en null no filtra. El CAST en ":param IS NULL" es necesario en
    // PostgreSQL: sin el, no puede deducir el tipo del parametro y la consulta falla con
    // "could not determine data type of parameter" (H2, la base de los tests, si lo deduce).
    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO(" +
            "c.idCita, c.fechaHoraProgramada, c.estado, " +
            "CONCAT(p.nombres, ' ', p.apellidos), " +
            "e.nombre) " +
            "FROM Citas c " +
            "JOIN c.paciente p " +
            "JOIN c.medico m " +
            "LEFT JOIN m.especialidad e " +
            "WHERE m.idUsuario = :idMedico " +
            "AND (CAST(:estado AS String) IS NULL OR c.estado = :estado) " +
            "AND (CAST(:inicio AS LocalDateTime) IS NULL OR c.fechaHoraProgramada >= :inicio) " +
            "AND (CAST(:fin AS LocalDateTime) IS NULL OR c.fechaHoraProgramada < :fin) " +
            "ORDER BY c.fechaHoraProgramada ASC")
    List<CitaMedicoDTO> findCitasDelMedico(@Param("idMedico") int idMedico,
                                           @Param("estado") String estado,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fin") LocalDateTime fin);

    // Reporte: citas de cada especialidad separadas por estado. La especialidad sale del
    // medico de la cita; si el medico no tiene una, se agrupa como "Sin especialidad".
    // Cada fila: [especialidad, atendidas, pendientes, canceladas, total]
    @Query(value = "SELECT COALESCE(e.nombre, 'Sin especialidad') AS especialidad, " +
            " SUM(CASE WHEN c.estado = 'ATENDIDA' THEN 1 ELSE 0 END) AS atendidas, " +
            " SUM(CASE WHEN c.estado = 'PENDIENTE' THEN 1 ELSE 0 END) AS pendientes, " +
            " SUM(CASE WHEN c.estado = 'CANCELADA' THEN 1 ELSE 0 END) AS canceladas, " +
            " COUNT(*) AS total " +
            " FROM citas c " +
            " INNER JOIN usuarios m ON m.id_usuario = c.medico_id " +
            " LEFT JOIN especialidades e ON e.id_especialidad = m.especialidad_id " +
            " GROUP BY COALESCE(e.nombre, 'Sin especialidad') " +
            " ORDER BY total DESC, especialidad ASC", nativeQuery = true)
    List<Object[]> contarCitasPorEspecialidadYEstado();

    // Reporte: tiempo de espera de los pacientes de cada medico. Solo cuenta citas ATENDIDAS,
    // porque las pendientes y canceladas todavia no tienen un tiempo de espera real.
    // El CAST a DECIMAL(10, 2) evita que el promedio de enteros se redondee a entero
    // (un NUMERIC sin escala pierde los decimales en algunas bases, como H2).
    // Cada fila: [medico, especialidad, citas atendidas, espera promedio, espera maxima]
    @Query(value = "SELECT CONCAT(m.nombres, ' ', m.apellidos) AS medico, " +
            " COALESCE(e.nombre, 'Sin especialidad') AS especialidad, " +
            " COUNT(c.id_cita) AS atendidas, " +
            " ROUND(AVG(CAST(c.tiempo_espera_minutos AS DECIMAL(10, 2))), 1) AS espera_promedio, " +
            " MAX(c.tiempo_espera_minutos) AS espera_maxima " +
            " FROM citas c " +
            " INNER JOIN usuarios m ON m.id_usuario = c.medico_id " +
            " LEFT JOIN especialidades e ON e.id_especialidad = m.especialidad_id " +
            " WHERE c.estado = 'ATENDIDA' " +
            " GROUP BY m.id_usuario, m.nombres, m.apellidos, e.nombre " +
            " ORDER BY espera_promedio DESC, medico ASC", nativeQuery = true)
    List<Object[]> obtenerEsperaPromedioPorMedico();
}
