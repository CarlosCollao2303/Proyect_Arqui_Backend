package pe.edu.upc.proyect_arqui_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRolesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RolesController.class)
@Import(SecurityConfig.class)
class RolesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IRolesService rS;

    @MockitoBean
    private IUsuariosService uS;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor token(String correo, String rol) {
        return jwt().jwt(j -> j.subject(correo)).authorities(new SimpleGrantedAuthority(rol));
    }

    @Test
    void admin_listar_200() throws Exception {
        when(rS.list()).thenReturn(List.of(new Roles(1, "ADMIN"), new Roles(2, "MEDICO")));

        mockMvc.perform(get("/roles/Listar").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void medico_listar_403() throws Exception {
        mockMvc.perform(get("/roles/Listar").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isForbidden());

        verify(rS, never()).list();
    }

    @Test
    void listar_sinToken_401() throws Exception {
        mockMvc.perform(get("/roles/Listar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void paciente_listarPorId_403() throws Exception {
        mockMvc.perform(get("/roles/ListarPorId/1").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_buscarPorNombre_200() throws Exception {
        when(rS.buscarPorNombre("med")).thenReturn(List.of(new Roles(2, "MEDICO")));

        mockMvc.perform(get("/roles/Buscar").param("nombre", " med ")
                        .with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("MEDICO"));

        verify(rS).buscarPorNombre("med");
    }

    @Test
    void buscar_nombreVacio_400() throws Exception {
        mockMvc.perform(get("/roles/Buscar").param("nombre", " ")
                        .with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isBadRequest());

        verify(rS, never()).buscarPorNombre(anyString());
    }
}
