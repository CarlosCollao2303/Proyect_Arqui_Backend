package pe.edu.upc.proyect_arqui_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Carga la SecurityConfig real para que se apliquen los @PreAuthorize.
@WebMvcTest(CitasController.class)
@Import(SecurityConfig.class)
class CitasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICitasService cS;

    @MockitoBean
    private IUsuariosService uS;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final CitaEstadoDTO estado = new CitaEstadoDTO(
            1, LocalDateTime.of(2026, 10, 5, 9, 30), "PENDIENTE",
            "Ana Torres", "Luis Rojas", "Cardiologia"
    );

    private static RequestPostProcessor token(String correo, String rol) {
        return jwt().jwt(j -> j.subject(correo)).authorities(new SimpleGrantedAuthority(rol));
    }

    @Test
    void admin_consultaCualquierCita_200() throws Exception {
        when(cS.consultarEstado(1, "admin@clinica.pe", true)).thenReturn(estado);

        mockMvc.perform(get("/citas/1/estado").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCita").value(1))
                .andExpect(jsonPath("$.fecha").value("2026-10-05"))
                .andExpect(jsonPath("$.hora").value("09:30:00"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.paciente").value("Ana Torres"))
                .andExpect(jsonPath("$.medico").value("Luis Rojas"))
                .andExpect(jsonPath("$.especialidad").value("Cardiologia"));
    }

    @Test
    void medico_puedeVerTodas_200() throws Exception {
        when(cS.consultarEstado(1, "luis@clinica.pe", true)).thenReturn(estado);

        mockMvc.perform(get("/citas/1/estado").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk());
    }

    @Test
    void paciente_consultaSuCita_200() throws Exception {
        when(cS.consultarEstado(1, "ana@mail.com", false)).thenReturn(estado);

        mockMvc.perform(get("/citas/1/estado").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(cS).consultarEstado(1, "ana@mail.com", false);
    }

    @Test
    void paciente_citaDeOtro_403() throws Exception {
        when(cS.consultarEstado(1, "otro@mail.com", false))
                .thenThrow(new AccessDeniedException("Solo puedes consultar el estado de tus propias citas"));

        mockMvc.perform(get("/citas/1/estado").with(token("otro@mail.com", "PACIENTE")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Solo puedes consultar el estado de tus propias citas"));
    }

    @Test
    void citaInexistente_404() throws Exception {
        when(cS.consultarEstado(99, "admin@clinica.pe", true))
                .thenThrow(new ResourceNotFoundException("La cita con id 99 no existe"));

        mockMvc.perform(get("/citas/99/estado").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("La cita con id 99 no existe"))
                .andExpect(jsonPath("$.path").value("/citas/99/estado"));
    }

    @Test
    void rolSinPermiso_403() throws Exception {
        mockMvc.perform(get("/citas/1/estado").with(token("x@mail.com", "RECEPCION")))
                .andExpect(status().isForbidden());

        verify(cS, never()).consultarEstado(anyInt(), anyString(), anyBoolean());
    }

    @Test
    void sinToken_401() throws Exception {
        mockMvc.perform(get("/citas/1/estado"))
                .andExpect(status().isUnauthorized());
    }
}
