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
import pe.edu.upc.proyect_arqui_backend.dtos.MedicoPorEspecialidadDTO;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IEspecialidadesService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EspecialidadesController.class)
@Import(SecurityConfig.class)
class EspecialidadesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IEspecialidadesService eS;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final List<MedicoPorEspecialidadDTO> medicos = List.of(
            new MedicoPorEspecialidadDTO(7, "Eva", "Diaz", "Cardiologia"),
            new MedicoPorEspecialidadDTO(5, "Luis", "Rojas", "Cardiologia")
    );

    private static RequestPostProcessor token(String correo, String rol) {
        return jwt().jwt(j -> j.subject(correo)).authorities(new SimpleGrantedAuthority(rol));
    }

    @Test
    void paciente_listaMedicosDeUnaEspecialidad_200() throws Exception {
        when(eS.listMedicosByEspecialidadId(1)).thenReturn(medicos);

        mockMvc.perform(get("/especialidades/1/medicos").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idMedico").value(7))
                .andExpect(jsonPath("$[0].nombres").value("Eva"))
                .andExpect(jsonPath("$[0].apellidos").value("Diaz"))
                .andExpect(jsonPath("$[0].especialidad").value("Cardiologia"));
    }

    @Test
    void admin_200() throws Exception {
        when(eS.listMedicosByEspecialidadId(1)).thenReturn(medicos);

        mockMvc.perform(get("/especialidades/1/medicos").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void medico_200() throws Exception {
        when(eS.listMedicosByEspecialidadId(1)).thenReturn(medicos);

        mockMvc.perform(get("/especialidades/1/medicos").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk());
    }

    @Test
    void especialidadSinMedicos_200ConListaVacia() throws Exception {
        when(eS.listMedicosByEspecialidadId(2)).thenReturn(List.of());

        mockMvc.perform(get("/especialidades/2/medicos").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void especialidadInexistente_404() throws Exception {
        when(eS.listMedicosByEspecialidadId(99))
                .thenThrow(new ResourceNotFoundException("La especialidad con id 99 no existe"));

        mockMvc.perform(get("/especialidades/99/medicos").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("La especialidad con id 99 no existe"))
                .andExpect(jsonPath("$.path").value("/especialidades/99/medicos"));
    }

    @Test
    void rolSinPermiso_403() throws Exception {
        mockMvc.perform(get("/especialidades/1/medicos").with(token("x@mail.com", "RECEPCION")))
                .andExpect(status().isForbidden());

        verify(eS, never()).listMedicosByEspecialidadId(anyInt());
    }

    @Test
    void sinToken_401() throws Exception {
        mockMvc.perform(get("/especialidades/1/medicos"))
                .andExpect(status().isUnauthorized());
    }
}
