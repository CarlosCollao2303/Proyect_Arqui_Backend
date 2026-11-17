package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDate;

// Lo que necesita el frontend para abrir la videollamada de una cita con la
// API iframe de Jitsi Meet: el dominio (para cargar external_api.js) y la sala.
public record VideollamadaDTO(
        int idCita,
        String dominio,
        String sala,
        String enlace,
        String paciente,
        String medico,
        LocalDate fecha,
        String estado
) {
}
