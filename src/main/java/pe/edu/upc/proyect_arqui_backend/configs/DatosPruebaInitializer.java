package pe.edu.upc.proyect_arqui_backend.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Solo para correr en local (app.preparar-bd-local=true): si la base no tiene usuarios,
// carga scripts/datos_prueba.sql al arrancar. Va despues de DataInitializer (@Order),
// porque el script necesita los roles que ese crea. En Render esta apagado.
@Component
@Order(2)
public class DatosPruebaInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosPruebaInitializer.class);
    private static final Path SCRIPT = Path.of("scripts", "datos_prueba.sql");

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.preparar-bd-local:false}")
    private boolean prepararBdLocal;

    public DatosPruebaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws IOException {
        if (!prepararBdLocal) {
            return;
        }

        Integer usuarios = jdbcTemplate.queryForObject("SELECT count(*) FROM usuarios", Integer.class);
        if (usuarios != null && usuarios > 0) {
            return;
        }

        if (!Files.exists(SCRIPT)) {
            log.warn("No se encontro {}: no se cargaron los datos de prueba", SCRIPT.toAbsolutePath());
            return;
        }

        jdbcTemplate.execute(Files.readString(SCRIPT, StandardCharsets.UTF_8));
        log.info("Datos de prueba cargados desde {}", SCRIPT);
    }
}
