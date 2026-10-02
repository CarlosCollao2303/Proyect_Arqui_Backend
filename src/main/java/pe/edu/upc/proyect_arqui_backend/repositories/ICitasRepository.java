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
    boolean existsByIdCitaAndPacienteCorreo(int idCita, String correo);

    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO(" +
            "c.idCita, c.fechaHoraProgramada, c.estado, " +
            "CONCAT(p.nombres, ' ', p.apellidos), " +
            "e.nombre) " +
            "FROM Citas c " +
            "JOIN c.paciente p " +
            "JOIN c.medico m " +
            "LEFT JOIN m.especialidad e " +
            "WHERE m.idUsuario = :idMedico " +
            "AND (:estado IS NULL OR c.estado = :estado) " +
            "AND (:inicio IS NULL OR c.fechaHoraProgramada >= :inicio) " +
            "AND (:fin IS NULL OR c.fechaHoraProgramada < :fin) " +
            "ORDER BY c.fechaHoraProgramada ASC")
    List<CitaMedicoDTO> findCitasDelMedico(@Param("idMedico") int idMedico,
                                           @Param("estado") String estado,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fin") LocalDateTime fin);
}
