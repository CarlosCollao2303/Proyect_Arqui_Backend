package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

public interface ICorreoService {
    // Envia un correo en formato HTML y devuelve el id que le asigna Resend.
    public String enviar(String destinatario, String asunto, String html);
}
