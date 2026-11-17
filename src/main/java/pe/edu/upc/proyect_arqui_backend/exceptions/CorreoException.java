package pe.edu.upc.proyect_arqui_backend.exceptions;

// Se lanza cuando no se pudo enviar un correo con Resend (API key sin configurar,
// remitente no permitido, Resend caido, etc.).
public class CorreoException extends RuntimeException {
    public CorreoException(String message) {
        super(message);
    }
}
