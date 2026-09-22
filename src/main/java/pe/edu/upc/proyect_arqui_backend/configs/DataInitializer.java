package pe.edu.upc.proyect_arqui_backend.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.repositories.IRolesRepository;
import pe.edu.upc.proyect_arqui_backend.repositories.IUsuariosRepository;

import java.util.List;

// Crea al arrancar los roles que usan los @PreAuthorize (si no existen) y, si se
// configuran app.admin.correo y app.admin.password, el primer usuario ADMIN.
// Sin esto nadie podria llegar a ser ADMIN: el registro publico siempre da PACIENTE.
@Component
public class DataInitializer implements CommandLineRunner {

    private static final List<String> ROLES = List.of("ADMIN", "MEDICO", "PACIENTE");

    private final IRolesRepository rR;
    private final IUsuariosRepository uR;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.correo:}")
    private String adminCorreo;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public DataInitializer(IRolesRepository rR, IUsuariosRepository uR, PasswordEncoder passwordEncoder) {
        this.rR = rR;
        this.uR = uR;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        for (String nombre : ROLES) {
            if (rR.findByNombre(nombre).isEmpty()) {
                rR.save(new Roles(0, nombre));
            }
        }

        if (adminCorreo.isBlank() || adminPassword.isBlank()
                || uR.findByCorreo(adminCorreo).isPresent()) {
            return;
        }

        Usuarios admin = new Usuarios();
        admin.setRol(rR.findByNombre("ADMIN").orElseThrow());
        admin.setNombres("Administrador");
        admin.setApellidos("Sistema");
        admin.setDni("00000000");
        admin.setTelefono("000000000");
        admin.setCorreo(adminCorreo);
        admin.setContrasenaHash(passwordEncoder.encode(adminPassword));
        admin.setEstado(true);
        uR.save(admin);
    }
}
