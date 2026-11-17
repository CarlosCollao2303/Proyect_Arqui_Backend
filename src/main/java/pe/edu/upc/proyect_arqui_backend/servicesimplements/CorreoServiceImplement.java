package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.upc.proyect_arqui_backend.exceptions.CorreoException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICorreoService;

@Service
public class CorreoServiceImplement implements ICorreoService {

    private final Resend resend;
    private final String apiKey;
    private final String remitente;

    public CorreoServiceImplement(Resend resend,
                                  @Value("${resend.api-key:}") String apiKey,
                                  @Value("${resend.remitente:SaludCerca <onboarding@resend.dev>}") String remitente) {
        this.resend = resend;
        this.apiKey = apiKey;
        this.remitente = remitente;
    }

    @Override
    public String enviar(String destinatario, String asunto, String html) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CorreoException(
                    "No se configuro la API key de Resend (variable de entorno RESEND_API_KEY)"
            );
        }

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(remitente)
                .to(destinatario)
                .subject(asunto)
                .html(html)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            return data.getId();
        } catch (ResendException | RuntimeException e) {
            // Ojo: cuando Resend responde con error HTTP (API key invalida, remitente
            // no permitido, etc.) el SDK lanza RuntimeException, no ResendException.
            throw new CorreoException("No se pudo enviar el correo: " + e.getMessage());
        }
    }
}
