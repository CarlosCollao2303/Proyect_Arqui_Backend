package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.dtos.MedicoPorEspecialidadDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;

import java.util.List;

public interface IEspecialidadesRepository extends JpaRepository<Especialidades, Integer> {
    List<Especialidades> findByNombreContainingIgnoreCase(String nombre);

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
}
