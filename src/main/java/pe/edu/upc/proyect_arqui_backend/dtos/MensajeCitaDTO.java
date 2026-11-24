package pe.edu.upc.proyect_arqui_backend.dtos;

// Mensaje pre armado de una cita, tal como se enviaria por correo. Sirve para la vista
// previa en el front: nadie tiene que escribirlo.
public record MensajeCitaDTO(
        int idCita,
        String tipo,
        String asunto,
        String destinatario,
        String correo,
        String mensaje
) {
}
