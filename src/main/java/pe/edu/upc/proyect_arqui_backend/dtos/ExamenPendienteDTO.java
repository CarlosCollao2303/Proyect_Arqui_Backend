package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExamenPendienteDTO(
        int idExamen,
        String tipoExamen,
        LocalDate fechaSolicitud
) {
    public ExamenPendienteDTO(int idExamen, String tipoExamen, LocalDateTime fechaSolicitud) {
        this(idExamen, tipoExamen, fechaSolicitud != null
                ? fechaSolicitud.toLocalDate() : null);
    }
}
