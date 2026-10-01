package pe.edu.upc.proyect_arqui_backend.dtos;

public record MedicoPorEspecialidadDTO(
        int idMedico,
        String nombres,
        String apellidos,
        String especialidad
) {
}
