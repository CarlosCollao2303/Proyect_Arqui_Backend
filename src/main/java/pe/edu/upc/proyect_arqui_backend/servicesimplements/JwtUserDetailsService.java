package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.repositories.IUsuariosRepository;

import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final IUsuariosRepository usuariosRepository;

    public JwtUserDetailsService(IUsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        Usuarios usuario = usuariosRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado: " + correo
                        )
                );

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(usuario.getRol().getNombre())
        );

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .authorities(authorities)
                .disabled(!usuario.isEstado())
                .build();
    }
}
