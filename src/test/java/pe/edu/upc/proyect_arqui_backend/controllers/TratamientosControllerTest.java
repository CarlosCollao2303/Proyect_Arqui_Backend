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
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ITratamientosService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TratamientosController.class)
@Import(SecurityConfig.class)
class TratamientosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITratamientosService tS;

    @MockitoBean
    private IDetalleHistorialService dhS;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor medico() {
        return jwt().jwt(j -> j.subject("luis@clinica.pe")).authorities(new SimpleGrantedAuthority("MEDICO"));
    }

    private static String tratamiento(String fechaInicio, String fechaFin) {
        return """
                {"idTratamiento": 1, "descripcion": "Fisioterapia", "fechaInicio": "%s",
                 "fechaFin": "%s", "estado": "ACTIVO"}
                """.formatted(fechaInicio, fechaFin);
    }

    @Test
    void registrar_fechaFinAntesDeInicio_400() throws Exception {
        mockMvc.perform(post("/tratamientos/Registrar").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tratamiento("2026-10-10", "2026-10-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La fecha de fin no puede ser anterior a la fecha de inicio"));

        verify(tS, never()).insert(any());
    }

    @Test
    void registrar_mismaFechaDeInicioYFin_201() throws Exception {
        mockMvc.perform(post("/tratamientos/Registrar").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tratamiento("2026-10-10", "2026-10-10")))
                .andExpect(status().isCreated());

        verify(tS).insert(any());
    }

    @Test
    void actualizar_fechaFinAntesDeInicio_400() throws Exception {
        mockMvc.perform(put("/tratamientos/Actualizar").with(medico())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tratamiento("2026-10-10", "2026-10-01")))
                .andExpect(status().isBadRequest());

        verify(tS, never()).listId(anyInt());
        verify(tS, never()).update(any());
    }
}
