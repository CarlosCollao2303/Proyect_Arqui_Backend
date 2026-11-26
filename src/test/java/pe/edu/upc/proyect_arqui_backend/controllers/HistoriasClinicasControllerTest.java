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
import pe.edu.upc.proyect_arqui_backend.dtos.CantidadDiagnosticosDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.HistorialClinicoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IHistoriasClinicasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoriasClinicasController.class)
@Import(SecurityConfig.class)
class HistoriasClinicasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IHistoriasClinicasService hS;

    @MockitoBean
    private IUsuariosService uS;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final Usuarios ana = new Usuarios(1, new Roles(3, "PACIENTE"), null, "Ana", "Torres", "12345678",
            "999999999", "ana@mail.com", "hash", null, null, true);
    private final HistoriasClinicas historia = new HistoriasClinicas(5, ana, LocalDateTime.of(2026, 1, 10, 0, 0));

    private static RequestPostProcessor token(String correo, String rol) {
        return jwt().jwt(j -> j.subject(correo)).authorities(new SimpleGrantedAuthority(rol));
    }

    // Lo que arma el servicio para la vista del paciente: sin diagnosticos ni tratamientos.
    private HistorialClinicoDTO vistaPaciente() {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        dto.setIdHistoriaClinica(5);
        dto.setPaciente("Ana Torres");
        dto.setCitas(List.of());
        dto.setExamenes(List.of());
        dto.setRecetas(List.of());
        return dto;
    }

    @Test
    void paciente_miHistorial_seBuscaConElIdDelToken_200() throws Exception {
        when(uS.listByCorreo("ana@mail.com")).thenReturn(Optional.of(ana));
        when(hS.listByPacienteId(1)).thenReturn(Optional.of(historia));
        when(hS.armarHistorial(historia, true)).thenReturn(vistaPaciente());

        mockMvc.perform(get("/api/historiasclinicas/mi-historial").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHistoriaClinica").value(5))
                .andExpect(jsonPath("$.citas").isArray())
                .andExpect(jsonPath("$.examenes").isArray())
                .andExpect(jsonPath("$.recetas").isArray())
                .andExpect(jsonPath("$.diagnosticos").doesNotExist())
                .andExpect(jsonPath("$.tratamientos").doesNotExist());

        verify(hS).armarHistorial(historia, true);
    }

    @Test
    void paciente_sinHistoria_404() throws Exception {
        when(uS.listByCorreo("ana@mail.com")).thenReturn(Optional.of(ana));
        when(hS.listByPacienteId(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/historiasclinicas/mi-historial").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Todavia no tienes una historia clinica registrada"));
    }

    @Test
    void medico_miHistorial_403() throws Exception {
        mockMvc.perform(get("/api/historiasclinicas/mi-historial").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void medico_historialCompleto_200() throws Exception {
        HistorialClinicoDTO completo = vistaPaciente();
        completo.setDiagnosticos(List.of());
        completo.setTratamientos(List.of());
        completo.setConsultas(List.of());
        when(hS.listId(5)).thenReturn(Optional.of(historia));
        when(hS.armarHistorial(historia, false)).thenReturn(completo);

        mockMvc.perform(get("/api/historiasclinicas/5/historial").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosticos").isArray())
                .andExpect(jsonPath("$.tratamientos").isArray());

        verify(hS).armarHistorial(historia, false);
    }

    @Test
    void paciente_historialDeOtraHistoria_403() throws Exception {
        mockMvc.perform(get("/api/historiasclinicas/5/historial").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isForbidden());

        verify(hS, never()).armarHistorial(any(), anyBoolean());
    }

    @Test
    void historialInexistente_404() throws Exception {
        when(hS.listId(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/historiasclinicas/99/historial").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isNotFound());
    }

    // ---- HU-24: cantidad de diagnosticos por historia clinica ----

    @Test
    void admin_cantidadDiagnosticosDeTodasLasHistorias_200() throws Exception {
        when(hS.contarDiagnosticosPorHistoria()).thenReturn(List.of(
                new CantidadDiagnosticosDTO(5, "Ana Torres", 3L),
                new CantidadDiagnosticosDTO(6, "Luis Gomez", 0L)));

        mockMvc.perform(get("/api/historiasclinicas/cantidad-diagnosticos").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idHistoriaClinica").value(5))
                .andExpect(jsonPath("$[0].paciente").value("Ana Torres"))
                .andExpect(jsonPath("$[0].cantidadDiagnosticos").value(3))
                .andExpect(jsonPath("$[1].cantidadDiagnosticos").value(0));
    }

    @Test
    void medico_cantidadDiagnosticosDeUnaHistoria_200() throws Exception {
        when(hS.contarDiagnosticosDeHistoria(5)).thenReturn(new CantidadDiagnosticosDTO(5, "Ana Torres", 3L));

        mockMvc.perform(get("/api/historiasclinicas/5/cantidad-diagnosticos").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHistoriaClinica").value(5))
                .andExpect(jsonPath("$.cantidadDiagnosticos").value(3));
    }

    @Test
    void historiaInexistente_cantidadDiagnosticos_404() throws Exception {
        when(hS.contarDiagnosticosDeHistoria(99))
                .thenThrow(new ResourceNotFoundException("No existe la historia clinica con el id: 99"));

        mockMvc.perform(get("/api/historiasclinicas/99/cantidad-diagnosticos").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void paciente_noPuedeVerCantidadDiagnosticos_403() throws Exception {
        mockMvc.perform(get("/api/historiasclinicas/cantidad-diagnosticos").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isForbidden());

        verify(hS, never()).contarDiagnosticosPorHistoria();
    }
}
