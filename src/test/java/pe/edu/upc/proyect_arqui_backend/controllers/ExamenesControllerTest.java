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
import pe.edu.upc.proyect_arqui_backend.dtos.CantidadExamenesDTO;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IExamenesService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExamenesController.class)
@Import(SecurityConfig.class)
class ExamenesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IExamenesService eS;

    @MockitoBean
    private IDetalleHistorialService dhS;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor medico() {
        return jwt().jwt(j -> j.subject("luis@clinica.pe")).authorities(new SimpleGrantedAuthority("MEDICO"));
    }

    private static String examen(String fechaSolicitud, String fechaResultado) {
        String resultado = fechaResultado == null ? "null" : "\"" + fechaResultado + "\"";
        return """
                {"idExamen": 1, "tipoExamen": "Hemograma", "fechaSolicitud": "%s",
                 "fechaResultado": %s}
                """.formatted(fechaSolicitud, resultado);
    }

    @Test
    void registrar_resultadoAntesDeLaSolicitud_400() throws Exception {
        mockMvc.perform(post("/api/examenes/Registrar").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examen("2026-10-10", "2026-10-09")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La fecha de resultado no puede ser anterior a la fecha de solicitud"));

        verify(eS, never()).insert(any());
    }

    @Test
    void registrar_sinResultadoQuedaPendiente_201() throws Exception {
        mockMvc.perform(post("/api/examenes/Registrar").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examen("2026-10-10", null)))
                .andExpect(status().isCreated());

        verify(eS).insert(any());
    }

    @Test
    void actualizar_resultadoAntesDeLaSolicitud_400() throws Exception {
        mockMvc.perform(put("/api/examenes/Actualizar").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examen("2026-10-10", "2026-10-09")))
                .andExpect(status().isBadRequest());

        verify(eS, never()).listId(anyInt());
        verify(eS, never()).update(any());
    }

    @Test
    void registrar_sinToken_401() throws Exception {
        mockMvc.perform(post("/api/examenes/Registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examen("2026-10-10", null)))
                .andExpect(status().isUnauthorized());

        verify(eS, never()).insert(any());
    }

    @Test
    void registrar_paciente_403() throws Exception {
        mockMvc.perform(post("/api/examenes/Registrar")
                        .with(jwt().jwt(j -> j.subject("ana@mail.com"))
                                .authorities(new SimpleGrantedAuthority("PACIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examen("2026-10-10", null)))
                .andExpect(status().isForbidden());

        verify(eS, never()).insert(any());
    }

    // ---- Reporte de examenes buscando al paciente por nombre ----

    @Test
    void cantidadPorNombreDePaciente_200() throws Exception {
        when(eS.contarExamenesPorNombreDePaciente("juan")).thenReturn(java.util.List.of(
                new CantidadExamenesDTO(9, "Juan Perez Garcia", "70000001", 3L)
        ));

        mockMvc.perform(get("/api/examenes/cantidad-por-paciente/buscar").param("nombre", " juan ")
                        .with(medico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paciente").value("Juan Perez Garcia"))
                .andExpect(jsonPath("$[0].cantidadExamenes").value(3));

        verify(eS).contarExamenesPorNombreDePaciente("juan");
    }

    @Test
    void cantidadPorNombreDePaciente_nombreVacio_400() throws Exception {
        mockMvc.perform(get("/api/examenes/cantidad-por-paciente/buscar").param("nombre", "  ")
                        .with(medico()))
                .andExpect(status().isBadRequest());

        verify(eS, never()).contarExamenesPorNombreDePaciente(anyString());
    }

    @Test
    void tiempoEntrega_convierteCadaFilaYRespetaLosDiasNull() throws Exception {
        // ROUND de PostgreSQL devuelve BigDecimal; un tipo sin resultados trae los dias en null
        when(eS.obtenerTiempoEntregaPorTipo()).thenReturn(java.util.List.<Object[]>of(
                new Object[]{"Resonancia", 2L, new java.math.BigDecimal("2"), new java.math.BigDecimal("0"),
                        new java.math.BigDecimal("4.5"), new java.math.BigDecimal("6.0")},
                new Object[]{"Urocultivo", 1L, 0L, 1L, null, null}));

        mockMvc.perform(get("/api/examenes/tiempo-entrega").with(medico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipoExamen").value("Resonancia"))
                .andExpect(jsonPath("$[0].solicitados").value(2))
                .andExpect(jsonPath("$[0].conResultado").value(2))
                .andExpect(jsonPath("$[0].pendientes").value(0))
                .andExpect(jsonPath("$[0].diasPromedio").value(4.5))
                .andExpect(jsonPath("$[0].diasMaximo").value(6.0))
                .andExpect(jsonPath("$[1].pendientes").value(1))
                .andExpect(jsonPath("$[1].diasPromedio").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$[1].diasMaximo").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void tiempoEntrega_paciente_403() throws Exception {
        mockMvc.perform(get("/api/examenes/tiempo-entrega")
                        .with(jwt().jwt(j -> j.subject("ana@mail.com")).authorities(new SimpleGrantedAuthority("PACIENTE"))))
                .andExpect(status().isForbidden());

        verify(eS, never()).obtenerTiempoEntregaPorTipo();
    }
}
