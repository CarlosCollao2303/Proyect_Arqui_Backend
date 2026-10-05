package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDateTime;

public record ExamenPendienteDTO(
        int idExamen,
        String tipoExamen,
        LocalDateTime fechaSolicitud
) {
}
