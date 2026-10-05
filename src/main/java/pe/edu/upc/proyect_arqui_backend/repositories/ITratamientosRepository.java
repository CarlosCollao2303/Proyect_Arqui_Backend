package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.dtos.TratamientoPacienteDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;

import java.time.LocalDate;
import java.util.List;

public interface ITratamientosRepository extends JpaRepository<Tratamientos, Integer> {
    List<Tratamientos> findByEstadoIgnoreCase(String estado);

    List<Tratamientos> findByFechaInicio(LocalDate fechaInicio);

    // Tratamientos vigentes en una fecha: ya empezaron y todavia no terminan, del que termina antes al que termina despues.
    @Query("SELECT t FROM Tratamientos t " +
            "WHERE t.fechaInicio <= :fecha AND t.fechaFin >= :fecha " +
            "ORDER BY t.fechaFin ASC")
    List<Tratamientos> listarTratamientosVigentes(@Param("fecha") LocalDate fecha);

    // Tratamientos de un paciente con el medico que los indico y el motivo de la consulta, del mas reciente al mas antiguo.
    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.TratamientoPacienteDTO(" +
            "t.idTratamiento, t.descripcion, t.fechaInicio, t.fechaFin, t.estado, " +
            "CONCAT(m.nombres, ' ', m.apellidos), d.motivoConsulta) " +
            "FROM DetalleHistorial d " +
            "INNER JOIN d.tratamiento t " +
            "INNER JOIN d.historiaClinica h " +
            "INNER JOIN h.paciente p " +
            "INNER JOIN d.medico m " +
            "WHERE p.idUsuario = :idPaciente " +
            "ORDER BY t.fechaInicio DESC")
    List<TratamientoPacienteDTO> listarTratamientosDePaciente(@Param("idPaciente") int idPaciente);
}
