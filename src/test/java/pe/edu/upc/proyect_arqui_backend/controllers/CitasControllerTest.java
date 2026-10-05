package pe.edu.upc.proyect_arqui_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private static final String MIS_CITAS_MEDICO = "/citas/medico/mis-citas";

    @Test
    void medico_listaSusCitas_200() throws Exception {
        when(cS.listarCitasDelMedico("luis@clinica.pe", null, null, null)).thenReturn(List.of(
                new CitaMedicoDTO(1, LocalDateTime.of(2026, 10, 5, 9, 30), "PENDIENTE",
                        "Ana Torres", "Cardiologia")
        ));

        mockMvc.perform(get(MIS_CITAS_MEDICO).with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCita").value(1))
                .andExpect(jsonPath("$[0].fecha").value("2026-10-05"))
                .andExpect(jsonPath("$[0].hora").value("09:30:00"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].paciente").value("Ana Torres"))
                .andExpect(jsonPath("$[0].especialidad").value("Cardiologia"));
    }

    @Test
    void medico_pasaFiltrosAlServiceConElCorreoDelToken() throws Exception {
        when(cS.listarCitasDelMedico(any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get(MIS_CITAS_MEDICO)
                        .param("estado", "ATENDIDA")
                        .param("inicio", "2026-10-01")
                        .param("fin", "2026-10-31")
                        .with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk());

        verify(cS).listarCitasDelMedico("luis@clinica.pe", "ATENDIDA",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    }

    @Test
    void medico_sinCitas_404() throws Exception {
        when(cS.listarCitasDelMedico("luis@clinica.pe", null, null, null))
                .thenThrow(new ResourceNotFoundException("No tienes citas programadas"));

        mockMvc.perform(get(MIS_CITAS_MEDICO).with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No tienes citas programadas"))
                .andExpect(jsonPath("$.path").value(MIS_CITAS_MEDICO));
    }

    @Test
    void medico_inicioPosteriorAFin_400() throws Exception {
        when(cS.listarCitasDelMedico("luis@clinica.pe", null,
                LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1)))
                .thenThrow(new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin"));

        mockMvc.perform(get(MIS_CITAS_MEDICO)
                        .param("inicio", "2026-10-31")
                        .param("fin", "2026-10-01")
                        .with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La fecha de inicio no puede ser posterior a la fecha de fin"));
    }

    @Test
    void medico_fechaConFormatoInvalido_400() throws Exception {
        mockMvc.perform(get(MIS_CITAS_MEDICO)
                        .param("inicio", "10-2026")
                        .with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El parametro 'inicio' tiene un formato invalido (use yyyy-MM-dd)"));

        verify(cS, never()).listarCitasDelMedico(any(), any(), any(), any());
    }

    @Test
    void paciente_noPuedeUsarMisCitasDeMedico_403() throws Exception {
        mockMvc.perform(get(MIS_CITAS_MEDICO).with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isForbidden());

        verify(cS, never()).listarCitasDelMedico(any(), any(), any(), any());
    }

    @Test
    void admin_noPuedeUsarMisCitasDeMedico_403() throws Exception {
        mockMvc.perform(get(MIS_CITAS_MEDICO).with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isForbidden());

        verify(cS, never()).listarCitasDelMedico(any(), any(), any(), any());
    }

    @Test
    void misCitasMedico_sinToken_401() throws Exception {
        mockMvc.perform(get(MIS_CITAS_MEDICO))
                .andExpect(status().isUnauthorized());
    }

    private static String cita(LocalDateTime fechaHora) {
        return """
                {"idMedico": 2, "fechaHoraProgramada": "%s"}
                """.formatted(fechaHora.withNano(0));
    }

    @Test
    void paciente_registraCitaEnElPasado_400() throws Exception {
        mockMvc.perform(post("/citas/Registrar").with(token("ana@mail.com", "PACIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita(LocalDateTime.now().minusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede registrar una cita en una fecha y hora pasada"));

        verify(cS, never()).insert(any());
    }

    @Test
    void paciente_registraCitaFutura_201() throws Exception {
        Usuarios ana = new Usuarios(1, new Roles(3, "PACIENTE"), null, "Ana", "Torres", "12345678",
                "999999999", "ana@mail.com", "hash", null, null, true);
        Usuarios luis = new Usuarios(2, new Roles(2, "MEDICO"), null, "Luis", "Rojas", "87654321",
                "988888888", "luis@clinica.pe", "hash", null, null, true);
        when(uS.listByCorreo("ana@mail.com")).thenReturn(Optional.of(ana));
        when(uS.listId(2)).thenReturn(Optional.of(luis));

        mockMvc.perform(post("/citas/Registrar").with(token("ana@mail.com", "PACIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita(LocalDateTime.now().plusDays(7))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPaciente").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(cS).insert(any());
    }
}
