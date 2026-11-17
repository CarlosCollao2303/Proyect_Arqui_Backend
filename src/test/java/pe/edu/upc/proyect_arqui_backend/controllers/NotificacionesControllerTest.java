package pe.edu.upc.proyect_arqui_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.CorreoException;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacionesController.class)
@Import(SecurityConfig.class)
class NotificacionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private INotificacionesService nS;

    @MockitoBean
    private IUsuariosService uS;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final Usuarios ana = new Usuarios(1, new Roles(3, "PACIENTE"), null, "Ana", "Torres", "12345678",
            "999999999", "ana@mail.com", "hash", null, null, true);

    private static final String CORREO = """
            {"idUsuario": 1, "tipo": "recordatorio", "mensaje": "  Recuerde su cita de mañana  "}
            """;

    @Test
    void enviarCorreo_usuarioExistente_201ConEstadoEnviado() throws Exception {
        when(uS.listId(1)).thenReturn(Optional.of(ana));
        when(nS.enviarPorCorreo(any(), anyString(), anyString(), anyString(), any()))
                .thenReturn(new Notificaciones(7, ana, "RECORDATORIO", "Recuerde su cita de mañana",
                        LocalDateTime.of(2026, 11, 16, 10, 0), Notificaciones.ESTADO_ENVIADO));

        mockMvc.perform(post("/api/notificaciones/EnviarCorreo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORREO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idNotificacion").value(7))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.estadoEnvio").value("ENVIADO"))
                .andExpect(jsonPath("$.fechaEnvio").value("2026-11-16"));

        // El tipo se normaliza a mayusculas, el asunto se arma con el tipo
        // y el mensaje llega sin espacios sobrantes.
        verify(nS).enviarPorCorreo(ana, "RECORDATORIO", "SaludCerca: recordatorio",
                "Recuerde su cita de mañana", null);
    }

    @Test
    void enviarCorreo_usuarioInexistente_404() throws Exception {
        when(uS.listId(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/notificaciones/EnviarCorreo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORREO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el usuario con el id: 1"));

        verify(nS, never()).enviarPorCorreo(any(), any(), any(), any(), any());
    }

    @Test
    void enviarCorreo_resendFalla_502ConElMotivo() throws Exception {
        when(uS.listId(1)).thenReturn(Optional.of(ana));
        when(nS.enviarPorCorreo(any(), anyString(), anyString(), anyString(), any()))
                .thenThrow(new CorreoException("No se pudo enviar el correo: API key is invalid"));

        mockMvc.perform(post("/api/notificaciones/EnviarCorreo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORREO))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("No se pudo enviar el correo: API key is invalid"));
    }

    @Test
    void enviarCorreo_sinMensaje_400() throws Exception {
        mockMvc.perform(post("/api/notificaciones/EnviarCorreo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idUsuario": 1, "tipo": "RECORDATORIO", "mensaje": ""}
                                """))
                .andExpect(status().isBadRequest());

        verify(nS, never()).enviarPorCorreo(any(), any(), any(), any(), any());
    }
}
