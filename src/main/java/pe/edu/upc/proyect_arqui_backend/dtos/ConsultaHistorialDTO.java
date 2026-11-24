package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDate;

// Una consulta del historial: cuando fue, que medico atendio y por que motivo.
public record ConsultaHistorialDTO(
        int idDetalleHistorial,
        LocalDate fecha,
        String medico,
        String motivoConsulta
) {
}
