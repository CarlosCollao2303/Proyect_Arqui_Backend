package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.dtos.MedicoPorEspecialidadDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;

import java.util.List;

public interface IEspecialidadesRepository extends JpaRepository<Especialidades, Integer> {
    List<Especialidades> findByNombreContainingIgnoreCase(String nombre);

    // Para comparar sin tildes: TRANSLATE cambia cada vocal con tilde (y la enie) por la
    // letra sin tilde. Existe en PostgreSQL y en H2 (la base de los tests).
    String CON_TILDES = "\u00e1\u00e9\u00ed\u00f3\u00fa\u00fc\u00f1";
    String SIN_TILDES = "aeiouun";

    // Como findByNombreContainingIgnoreCase, pero "cardiologia" tambien encuentra "Cardiolog\u00eda".
    @Query("SELECT e FROM Especialidades e " +
            "WHERE CAST(FUNCTION('TRANSLATE', LOWER(e.nombre), '" + CON_TILDES + "', '" + SIN_TILDES + "') AS String) " +
            "LIKE CONCAT('%', CAST(FUNCTION('TRANSLATE', LOWER(:nombre), '" + CON_TILDES + "', '" + SIN_TILDES + "') AS String), '%') " +
            "ORDER BY e.nombre ASC")
    List<Especialidades> buscarPorNombreSinTildes(@Param("nombre") String nombre);

    @Query("SELECT COUNT(u) FROM Usuarios u " +
            "WHERE u.especialidad.idEspecialidad = :idEspecialidad " +
            "AND u.rol.nombre = 'MEDICO'")
    long countMedicosByEspecialidadId(@Param("idEspecialidad") int idEspecialidad);

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.MedicoPorEspecialidadDTO(" +
            "u.idUsuario, u.nombres, u.apellidos, e.nombre) " +
            "FROM Usuarios u " +
            "JOIN u.especialidad e " +
            "JOIN u.rol r " +
            "WHERE e.idEspecialidad = :idEspecialidad " +
            "AND r.nombre = 'MEDICO' " +
            "AND u.estado = true " +
            "ORDER BY u.apellidos ASC, u.nombres ASC")
    List<MedicoPorEspecialidadDTO> findMedicosByEspecialidadId(@Param("idEspecialidad") int idEspecialidad);

    // Todos los medicos activos. Los que aun no tienen especialidad salen como "Sin especialidad".
    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.MedicoPorEspecialidadDTO(" +
            "u.idUsuario, u.nombres, u.apellidos, COALESCE(e.nombre, 'Sin especialidad')) " +
            "FROM Usuarios u " +
            "JOIN u.rol r " +
            "LEFT JOIN u.especialidad e " +
            "WHERE r.nombre = 'MEDICO' " +
            "AND u.estado = true " +
            "ORDER BY u.apellidos ASC, u.nombres ASC")
    List<MedicoPorEspecialidadDTO> findTodosLosMedicos();

    // Especialidades que tienen al menos un medico activo: las que el paciente puede elegir.
    @Query("SELECT DISTINCT e FROM Usuarios u " +
            "JOIN u.especialidad e " +
            "JOIN u.rol r " +
            "WHERE r.nombre = 'MEDICO' " +
            "AND u.estado = true " +
            "ORDER BY e.nombre ASC")
    List<Especialidades> findEspecialidadesConMedicos();

    // Reporte: medicos de cada especialidad, separados en activos e inactivos.
    // Incluye las especialidades sin medicos (salen con 0) y, si hay medicos que aun no
    // tienen especialidad, una fila "Sin especialidad" con id null.
    // Cada fila: [idEspecialidad, especialidad, activos, inactivos, total]
    @Query(value = "SELECT * FROM (" +
            " SELECT e.id_especialidad AS id_especialidad, e.nombre AS especialidad, " +
            "  SUM(CASE WHEN m.estado = TRUE THEN 1 ELSE 0 END) AS activos, " +
            "  SUM(CASE WHEN m.estado = FALSE THEN 1 ELSE 0 END) AS inactivos, " +
            "  COUNT(m.id_usuario) AS total " +
            " FROM especialidades e " +
            " LEFT JOIN usuarios m ON m.especialidad_id = e.id_especialidad " +
            "  AND m.rol_id IN (SELECT r.id_rol FROM roles r WHERE r.nombre = 'MEDICO') " +
            " GROUP BY e.id_especialidad, e.nombre " +
            " UNION ALL " +
            " SELECT CAST(NULL AS INTEGER), 'Sin especialidad', " +
            "  SUM(CASE WHEN m.estado = TRUE THEN 1 ELSE 0 END), " +
            "  SUM(CASE WHEN m.estado = FALSE THEN 1 ELSE 0 END), " +
            "  COUNT(m.id_usuario) " +
            " FROM usuarios m " +
            " INNER JOIN roles r ON r.id_rol = m.rol_id " +
            " WHERE r.nombre = 'MEDICO' AND m.especialidad_id IS NULL " +
            " HAVING COUNT(m.id_usuario) > 0 " +
            ") reporte " +
            "ORDER BY activos DESC, especialidad ASC", nativeQuery = true)
    List<Object[]> contarMedicosPorEspecialidad();
}
