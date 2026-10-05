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
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IExamenesService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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
        mockMvc.perform(post("/examenes/Registrar").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examen("2026-10-10T09:00:00", "2026-10-09T09:00:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La fecha de resultado no puede ser anterior a la fecha de solicitud"));

        verify(eS, never()).insert(any());
    }

    @Test
    void registrar_sinResultadoQuedaPendiente_201() throws Exception {
        mockMvc.perform(post("/examenes/Registrar").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examen("2026-10-10T09:00:00", null)))
                .andExpect(status().isCreated());

        verify(eS).insert(any());
    }

    @Test
    void actualizar_resultadoAntesDeLaSolicitud_400() throws Exception {
        mockMvc.perform(put("/examenes/Actualizar").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examen("2026-10-10T09:00:00", "2026-10-09T09:00:00")))
                .andExpect(status().isBadRequest());

        verify(eS, never()).listId(anyInt());
        verify(eS, never()).update(any());
    }
}
