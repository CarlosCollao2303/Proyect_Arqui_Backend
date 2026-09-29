package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Respuesta de GET /citas/{idCita}/estado (HU-10). Lo arma directamente la query
// ICitasRepository.findEstadoById, por eso tiene el constructor extra.
public record CitaEstadoDTO(
        int idCita,
        LocalDate fecha,
        LocalTime hora,
        String estado,
        String paciente,
        String medico,
        String especialidad
) {
    // Usado por el "SELECT new" de la query: en la BD fecha y hora van juntas.
    public CitaEstadoDTO(int idCita, LocalDateTime fechaHoraProgramada, String estado,
                         String paciente, String medico, String especialidad) {
        this(
                idCita,
                fechaHoraProgramada != null ? fechaHoraProgramada.toLocalDate() : null,
                fechaHoraProgramada != null ? fechaHoraProgramada.toLocalTime() : null,
                estado,
                paciente,
                medico,
                especialidad
        );
    }
}
