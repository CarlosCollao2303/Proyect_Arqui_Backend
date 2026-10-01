package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;

import java.util.List;

public interface IEspecialidadesRepository extends JpaRepository<Especialidades, Integer> {
    List<Especialidades> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT COUNT(u) FROM Usuarios u " +
            "WHERE u.especialidad.idEspecialidad = :idEspecialidad " +
            "AND u.rol.nombre = 'MEDICO'")
    long countMedicosByEspecialidadId(@Param("idEspecialidad") int idEspecialidad);
}
