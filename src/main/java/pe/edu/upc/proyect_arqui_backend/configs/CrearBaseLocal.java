package pe.edu.upc.proyect_arqui_backend.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Solo para correr en local (app.preparar-bd-local=true): antes de que Hibernate se
// conecte, crea la base de PostgreSQL de spring.datasource.url si todavia no existe.
// Asi nadie tiene que crearla a mano con pgAdmin. En Render esta apagado.
@Component
public class CrearBaseLocal implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final Logger log = LoggerFactory.getLogger(CrearBaseLocal.class);
    private static final Pattern URL_POSTGRES = Pattern.compile("^(jdbc:postgresql://[^/]+/)([^?]+)(.*)$");

    private Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        if (!env.getProperty("app.preparar-bd-local", Boolean.class, false)) {
            return;
        }

        Matcher url = URL_POSTGRES.matcher(env.getProperty("spring.datasource.url", ""));
        if (!url.matches()) {
            return;
        }
        String nombreBase = url.group(2);
        // Se conecta a la base "postgres", que siempre existe, para crear la del proyecto
        String urlMantenimiento = url.group(1) + "postgres" + url.group(3);

        try (Connection conexion = DriverManager.getConnection(urlMantenimiento,
                env.getProperty("spring.datasource.username"),
                env.getProperty("spring.datasource.password"));
             PreparedStatement existe = conexion.prepareStatement(
                     "SELECT 1 FROM pg_database WHERE datname = ?")) {
            existe.setString(1, nombreBase);
            try (ResultSet rs = existe.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
            try (Statement crear = conexion.createStatement()) {
                crear.execute("CREATE DATABASE \"" + nombreBase.replace("\"", "\"\"") + "\"");
            }
            log.info("Se creo la base local {}", nombreBase);
        } catch (SQLException e) {
            log.warn("No se pudo crear la base local {}: {}", nombreBase, e.getMessage());
        }
    }
}
