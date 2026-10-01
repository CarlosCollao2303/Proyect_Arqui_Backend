package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CitaMedicoDTO(
        int idCita,
        LocalDate fecha,
        LocalTime hora,
        String estado,
        String paciente,
        String especialidad
) {
    public CitaMedicoDTO(int idCita, LocalDateTime fechaHoraProgramada, String estado,
                         String paciente, String especialidad) {
        this(
                idCita,
                fechaHoraProgramada != null ? fechaHoraProgramada.toLocalDate() : null,
                fechaHoraProgramada != null ? fechaHoraProgramada.toLocalTime() : null,
                estado,
                paciente,
                especialidad
        );
    }
}
