package pe.edu.upc.proyect_arqui_backend.configs;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Cliente de Resend para enviar correos. La API key sale de la propiedad
// resend.api-key (variable de entorno RESEND_API_KEY). Si esta vacia la app
// igual arranca: el error aparece recien al intentar enviar un correo.
@Configuration
public class ResendConfig {
    @Value("${resend.api-key:}")
    private String apiKey;

    @Bean
    public Resend resend() {
        return new Resend(apiKey);
    }
}
