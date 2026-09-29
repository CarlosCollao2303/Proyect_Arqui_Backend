package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;

import java.util.List;
import java.util.Optional;

public interface ICitasRepository extends JpaRepository<Citas, Integer> {
    // Citas donde el usuario es el paciente o el medico, de la mas proxima a la mas lejana.
    @Query("SELECT c FROM Citas c " +
            "WHERE c.paciente.correo = :correo OR c.medico.correo = :correo " +
            "ORDER BY c.fechaHoraProgramada ASC")
    List<Citas> findByParticipanteCorreo(@Param("correo") String correo);

    // HU-10: estado de una cita con los nombres de paciente, medico y especialidad.
    // LEFT JOIN en especialidad porque en Usuarios puede ser null.
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

    boolean existsByIdCitaAndPacienteCorreo(int idCita, String correo);
}
