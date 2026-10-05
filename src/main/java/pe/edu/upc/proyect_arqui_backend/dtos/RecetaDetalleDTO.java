package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDateTime;

public record RecetaDetalleDTO(
        int idReceta,
        String paciente,
        String medico,
        String motivoConsulta,
        String medicamento,
        String dosis,
        String frecuencia,
        int duracionDias,
        LocalDateTime fechaEmision
) {
}
