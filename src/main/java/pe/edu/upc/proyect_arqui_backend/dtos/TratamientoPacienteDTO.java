package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDate;

public record TratamientoPacienteDTO(
        int idTratamiento,
        String descripcion,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado,
        String medico,
        String motivoConsulta
) {
}
