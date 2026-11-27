package pe.edu.upc.proyect_arqui_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upc.proyect_arqui_backend.securities.JwtTokenService;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDiagnosticoService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IEspecialidadesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IExamenesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IHistoriasClinicasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRecetasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRolesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ITratamientosService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        DetalleHistorialController.class,
        DiagnosticoController.class,
        HistoriasClinicasController.class,
        LoginController.class,
        NotificacionesController.class,
        RecetasController.class,
        RolesController.class,
        UsuariosController.class
})
@Import(SecurityConfig.class)
class PublicControllersSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDetalleHistorialService detalleHistorialService;

    @MockitoBean
    private IDiagnosticoService diagnosticoService;

    @MockitoBean
    private IHistoriasClinicasService historiasClinicasService;

    @MockitoBean
    private INotificacionesService notificacionesService;

    @MockitoBean
    private ICitasService citasService;

    @MockitoBean
    private IRecetasService recetasService;

    @MockitoBean
    private IRolesService rolesService;

    @MockitoBean
    private IUsuariosService usuariosService;

    @MockitoBean
    private IEspecialidadesService especialidadesService;

    @MockitoBean
    private IExamenesService examenesService;

    @MockitoBean
    private ITratamientosService tratamientosService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void detalleHistorial_listarSinToken_401() throws Exception {
        mockMvc.perform(get("/api/detalles-historial/Listar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void diagnosticos_listarSinToken_401() throws Exception {
        mockMvc.perform(get("/api/diagnosticos/Listar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void historiasClinicas_listarSinToken_401() throws Exception {
        mockMvc.perform(get("/api/historias-clinicas/Listar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_generaTokenSinTokenPrevio() throws Exception {
        var user = User.withUsername("medico@clinica.pe")
                .password("hash")
                .authorities("MEDICO")
                .build();
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenService.generateToken(user)).thenReturn("jwt-test");

        mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"correo":"medico@clinica.pe","password":"secreto"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-test"))
                .andExpect(jsonPath("$.username").value("medico@clinica.pe"));

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void notificaciones_listarSinToken_401() throws Exception {
        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recetas_listarSinToken_401() throws Exception {
        mockMvc.perform(get("/api/recetas/Listar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roles_listarSinToken_401() throws Exception {
        mockMvc.perform(get("/roles/Listar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarios_listarSinToken_401() throws Exception {
        mockMvc.perform(get("/api/usuarios/Listar"))
                .andExpect(status().isUnauthorized());
    }
}
