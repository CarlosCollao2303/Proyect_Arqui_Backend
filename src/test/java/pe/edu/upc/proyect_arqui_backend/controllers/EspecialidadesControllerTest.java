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
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;
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

        mockMvc.perform(get("/api/especialidades/1/medicos").with(token("ana@mail.com", "PACIENTE")))
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

        mockMvc.perform(get("/api/especialidades/1/medicos").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void medico_200() throws Exception {
        when(eS.listMedicosByEspecialidadId(1)).thenReturn(medicos);

        mockMvc.perform(get("/api/especialidades/1/medicos").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk());
    }

    @Test
    void especialidadSinMedicos_200ConListaVacia() throws Exception {
        when(eS.listMedicosByEspecialidadId(2)).thenReturn(List.of());

        mockMvc.perform(get("/api/especialidades/2/medicos").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void especialidadInexistente_404() throws Exception {
        when(eS.listMedicosByEspecialidadId(99))
                .thenThrow(new ResourceNotFoundException("La especialidad con id 99 no existe"));

        mockMvc.perform(get("/api/especialidades/99/medicos").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("La especialidad con id 99 no existe"))
                .andExpect(jsonPath("$.path").value("/api/especialidades/99/medicos"));
    }

    @Test
    void rolSinPermiso_403() throws Exception {
        mockMvc.perform(get("/api/especialidades/1/medicos").with(token("x@mail.com", "RECEPCION")))
                .andExpect(status().isForbidden());

        verify(eS, never()).listMedicosByEspecialidadId(anyInt());
    }

    @Test
    void sinToken_401() throws Exception {
        mockMvc.perform(get("/api/especialidades/1/medicos"))
                .andExpect(status().isUnauthorized());

        verify(eS, never()).listMedicosByEspecialidadId(anyInt());
    }

    // ---- Consultas que ahora tienen @PreAuthorize ----

    @Test
    void paciente_listaEspecialidades_200() throws Exception {
        when(eS.list()).thenReturn(List.of(new Especialidades(1, "Cardiologia")));

        mockMvc.perform(get("/api/especialidades/Listar").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Cardiologia"));
    }

    @Test
    void listarEspecialidades_rolSinPermiso_403() throws Exception {
        mockMvc.perform(get("/api/especialidades/Listar").with(token("x@mail.com", "RECEPCION")))
                .andExpect(status().isForbidden());

        verify(eS, never()).list();
    }

    @Test
    void buscarPorNombre_sinToken_401() throws Exception {
        mockMvc.perform(get("/api/especialidades/Buscar").param("nombre", "cardio"))
                .andExpect(status().isUnauthorized());
    }

    // ---- Todos los medicos y especialidades disponibles para el paciente ----

    @Test
    void paciente_todosLosMedicos_200() throws Exception {
        when(eS.listTodosLosMedicos()).thenReturn(List.of(
                new MedicoPorEspecialidadDTO(7, "Eva", "Diaz", "Cardiologia"),
                new MedicoPorEspecialidadDTO(8, "Rosa", "Castillo", "Sin especialidad")
        ));

        mockMvc.perform(get("/api/especialidades/medicos").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].especialidad").value("Sin especialidad"));
    }

    @Test
    void paciente_especialidadesConMedicos_200() throws Exception {
        when(eS.listEspecialidadesConMedicos()).thenReturn(List.of(new Especialidades(3, "Cardiologia")));

        mockMvc.perform(get("/api/especialidades/con-medicos").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idEspecialidad").value(3));
    }

    @Test
    void todosLosMedicos_sinToken_401() throws Exception {
        mockMvc.perform(get("/api/especialidades/medicos"))
                .andExpect(status().isUnauthorized());

        verify(eS, never()).listTodosLosMedicos();
    }

    // ---- Reporte de medicos por especialidad (GET /api/especialidades/reporte-medicos) ----

    private List<Object[]> filasReporte() {
        return List.of(
                new Object[]{1, "Cardiologia", 3L, 1L, 4L},
                new Object[]{2, "Pediatria", 0L, 0L, 0L},
                new Object[]{null, "Sin especialidad", 2L, 0L, 2L}
        );
    }

    @Test
    void admin_reporteMedicosPorEspecialidad_200ConCantidades() throws Exception {
        when(eS.contarMedicosPorEspecialidad()).thenReturn(filasReporte());

        mockMvc.perform(get("/api/especialidades/reporte-medicos").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].idEspecialidad").value(1))
                .andExpect(jsonPath("$[0].especialidad").value("Cardiologia"))
                .andExpect(jsonPath("$[0].activos").value(3))
                .andExpect(jsonPath("$[0].inactivos").value(1))
                .andExpect(jsonPath("$[0].total").value(4))
                .andExpect(jsonPath("$[1].activos").value(0))
                .andExpect(jsonPath("$[2].idEspecialidad").doesNotExist())
                .andExpect(jsonPath("$[2].especialidad").value("Sin especialidad"));
    }

    @Test
    void admin_reporteSinEspecialidades_200ConListaVacia() throws Exception {
        when(eS.contarMedicosPorEspecialidad()).thenReturn(List.of());

        mockMvc.perform(get("/api/especialidades/reporte-medicos").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void medico_noPuedeVerElReporteDeMedicos_403() throws Exception {
        mockMvc.perform(get("/api/especialidades/reporte-medicos").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isForbidden());

        verify(eS, never()).contarMedicosPorEspecialidad();
    }

    @Test
    void paciente_noPuedeVerElReporteDeMedicos_403() throws Exception {
        mockMvc.perform(get("/api/especialidades/reporte-medicos").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isForbidden());

        verify(eS, never()).contarMedicosPorEspecialidad();
    }

    @Test
    void sinToken_reporteDeMedicos_401() throws Exception {
        mockMvc.perform(get("/api/especialidades/reporte-medicos"))
                .andExpect(status().isUnauthorized());

        verify(eS, never()).contarMedicosPorEspecialidad();
    }
}
