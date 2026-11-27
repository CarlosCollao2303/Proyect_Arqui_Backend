package pe.edu.upc.proyect_arqui_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pe.edu.upc.proyect_arqui_backend.dtos.MensajeCitaDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.CorreoException;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.INotificacionesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private ICitasService cS;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final Usuarios ana = new Usuarios(1, new Roles(3, "PACIENTE"), null, "Ana", "Torres", "12345678",
            "999999999", "ana@mail.com", "hash", null, null, true);

    private static final String CORREO = """
            {"idUsuario": 1, "tipo": "recordatorio", "mensaje": "  Recuerde su cita de mañana  "}
            """;

    private static RequestPostProcessor token(String correo, String rol) {
        return jwt().jwt(j -> j.subject(correo)).authorities(new SimpleGrantedAuthority(rol));
    }

    private static RequestPostProcessor medico() {
        return token("luis@clinica.pe", "MEDICO");
    }

    @Test
    void enviarCorreo_usuarioExistente_201ConEstadoEnviado() throws Exception {
        when(uS.listId(1)).thenReturn(Optional.of(ana));
        when(nS.enviarPorCorreo(any(), anyString(), anyString(), anyString(), any()))
                .thenReturn(new Notificaciones(7, ana, "RECORDATORIO", "Recuerde su cita de mañana",
                        LocalDateTime.of(2026, 11, 16, 10, 0), Notificaciones.ESTADO_ENVIADO));

        mockMvc.perform(post("/api/notificaciones/EnviarCorreo").with(medico())
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

        mockMvc.perform(post("/api/notificaciones/EnviarCorreo").with(medico())
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

        mockMvc.perform(post("/api/notificaciones/EnviarCorreo").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORREO))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("No se pudo enviar el correo: API key is invalid"));
    }

    @Test
    void enviarCorreo_sinMensaje_400() throws Exception {
        mockMvc.perform(post("/api/notificaciones/EnviarCorreo").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idUsuario": 1, "tipo": "RECORDATORIO", "mensaje": ""}
                                """))
                .andExpect(status().isBadRequest());

        verify(nS, never()).enviarPorCorreo(any(), any(), any(), any(), any());
    }

    @Test
    void enviarCorreo_sinToken_401() throws Exception {
        mockMvc.perform(post("/api/notificaciones/EnviarCorreo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORREO))
                .andExpect(status().isUnauthorized());

        verify(nS, never()).enviarPorCorreo(any(), any(), any(), any(), any());
    }

    @Test
    void enviarCorreo_paciente_403() throws Exception {
        mockMvc.perform(post("/api/notificaciones/EnviarCorreo").with(token("ana@mail.com", "PACIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORREO))
                .andExpect(status().isForbidden());

        verify(nS, never()).enviarPorCorreo(any(), any(), any(), any(), any());
    }

    // ---- Notificacion de una cita con mensaje pre armado ----

    private final Usuarios luis = new Usuarios(2, new Roles(2, "MEDICO"), null, "Luis", "Rojas", "87654321",
            "988888888", "luis@clinica.pe", "hash", null, null, true);

    private Citas citaPendiente() {
        Citas cita = new Citas(16, ana, luis, LocalDateTime.of(2026, 11, 24, 0, 0), 0, "PENDIENTE");
        cita.setSalaVideollamada("SaludCerca-abc123");
        return cita;
    }

    @Test
    void vistaPrevia_medicoDeLaCita_200ConElMensajeArmado() throws Exception {
        when(cS.listId(16)).thenReturn(Optional.of(citaPendiente()));
        when(nS.armarMensajeCita(any(Citas.class), isNull()))
                .thenReturn(new MensajeCitaDTO(16, "RECORDATORIO", "SaludCerca: recordatorio de tu cita del 24/11/2026",
                        "Ana Torres", "ana@mail.com", "Te recordamos tu cita #16 con Luis Rojas"));

        mockMvc.perform(get("/api/notificaciones/VistaPrevia/16").with(medico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("RECORDATORIO"))
                .andExpect(jsonPath("$.destinatario").value("Ana Torres"))
                .andExpect(jsonPath("$.mensaje").value("Te recordamos tu cita #16 con Luis Rojas"));

        // La vista previa no envia nada
        verify(nS, never()).notificarCita(any(), any(), any());
    }

    @Test
    void vistaPrevia_medicoDeOtraCita_403() throws Exception {
        when(cS.listId(16)).thenReturn(Optional.of(citaPendiente()));

        mockMvc.perform(get("/api/notificaciones/VistaPrevia/16").with(token("eva@clinica.pe", "MEDICO")))
                .andExpect(status().isForbidden());

        verify(nS, never()).armarMensajeCita(any(), any());
    }

    @Test
    void enviarPorCita_admin_201SinEscribirElMensaje() throws Exception {
        when(cS.listId(16)).thenReturn(Optional.of(citaPendiente()));
        when(nS.notificarCita(any(Citas.class), isNull(), eq("https://meet.jit.si/SaludCerca-abc123")))
                .thenReturn(new Notificaciones(9, ana, "RECORDATORIO", "Te recordamos tu cita #16",
                        LocalDateTime.of(2026, 11, 24, 8, 0), Notificaciones.ESTADO_ENVIADO));

        mockMvc.perform(post("/api/notificaciones/EnviarPorCita").with(token("admin@clinica.pe", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCita": 16}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idNotificacion").value(9))
                .andExpect(jsonPath("$.tipo").value("RECORDATORIO"))
                .andExpect(jsonPath("$.estadoEnvio").value("ENVIADO"));
    }

    @Test
    void enviarPorCita_sinIdCita_400() throws Exception {
        mockMvc.perform(post("/api/notificaciones/EnviarPorCita").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(nS, never()).notificarCita(any(), any(), any());
    }

    @Test
    void enviarPorCita_citaInexistente_404() throws Exception {
        when(cS.listId(99)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/notificaciones/EnviarPorCita").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCita": 99}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe una cita con el id: 99"));
    }

    @Test
    void contarPorEstado_medico_200() throws Exception {
        when(nS.contarNotificacionesPorEstado()).thenReturn(java.util.List.of(
                new pe.edu.upc.proyect_arqui_backend.dtos.ConteoNotificacionesEstadoDTO("ENVIADO", 5L),
                new pe.edu.upc.proyect_arqui_backend.dtos.ConteoNotificacionesEstadoDTO("FALLIDO", 2L)
        ));

        mockMvc.perform(get("/api/notificaciones/PorEstado").with(medico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoEnvio").value("ENVIADO"))
                .andExpect(jsonPath("$[0].total").value(5))
                .andExpect(jsonPath("$[1].estadoEnvio").value("FALLIDO"))
                .andExpect(jsonPath("$[1].total").value(2));
    }

    @Test
    void enviarPorCita_paciente_403() throws Exception {
        mockMvc.perform(post("/api/notificaciones/EnviarPorCita").with(token("ana@mail.com", "PACIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCita": 16}
                                """))
                .andExpect(status().isForbidden());

        verify(nS, never()).notificarCita(any(), any(), any());
    }
}
