package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaPacienteDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;

import java.time.LocalDateTime;
import java.util.List;

public interface ICitasRepository extends JpaRepository<Citas, Integer> {

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
}
