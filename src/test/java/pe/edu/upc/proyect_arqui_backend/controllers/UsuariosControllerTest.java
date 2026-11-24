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
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.securities.SecurityConfig;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IEspecialidadesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRolesService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuariosController.class)
@Import(SecurityConfig.class)
class UsuariosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUsuariosService uS;

    @MockitoBean
    private IRolesService rS;

    @MockitoBean
    private IEspecialidadesService eS;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final Usuarios ana = new Usuarios(1, new Roles(3, "PACIENTE"), null, "Ana", "Torres", "12345678",
            "999999999", "ana@mail.com", "hash", null, null, true);
    private final Usuarios luis = new Usuarios(2, new Roles(2, "MEDICO"), null, "Luis", "Rojas", "87654321",
            "988888888", "luis@clinica.pe", "hash", null, null, true);

    private static RequestPostProcessor token(String correo, String rol) {
        return jwt().jwt(j -> j.subject(correo)).authorities(new SimpleGrantedAuthority(rol));
    }

    // ---- Mis pacientes del medico ----

    @Test
    void medico_misPacientes_soloLosDeSusCitas_200() throws Exception {
        when(uS.listPacientesDelMedico("luis@clinica.pe")).thenReturn(List.of(ana));

        mockMvc.perform(get("/api/usuarios/mis-pacientes").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombres").value("Ana"))
                .andExpect(jsonPath("$[0].contrasenaHash").doesNotExist());

        verify(uS).listPacientesDelMedico("luis@clinica.pe");
    }

    @Test
    void admin_misPacientes_403() throws Exception {
        mockMvc.perform(get("/api/usuarios/mis-pacientes").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isForbidden());

        verify(uS, never()).listPacientesDelMedico(any());
    }

    @Test
    void medico_todosLosPacientes_200() throws Exception {
        when(uS.listByRol("PACIENTE")).thenReturn(List.of(ana));

        mockMvc.perform(get("/api/usuarios/pacientes").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idRol").value(3));
    }

    @Test
    void paciente_listaDePacientes_403() throws Exception {
        mockMvc.perform(get("/api/usuarios/pacientes").with(token("ana@mail.com", "PACIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_medicos_200() throws Exception {
        when(uS.listByRol("MEDICO")).thenReturn(List.of(luis));

        mockMvc.perform(get("/api/usuarios/medicos").with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("Luis"));
    }

    // ---- Busqueda por nombre o DNI ----

    @Test
    void medico_buscar_soloEntrePacientes() throws Exception {
        when(uS.buscarPorNombreODni("ana", "PACIENTE")).thenReturn(List.of(ana));

        mockMvc.perform(get("/api/usuarios/Buscar").param("texto", " ana ")
                        .with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(uS).buscarPorNombreODni("ana", "PACIENTE");
    }

    @Test
    void admin_buscar_entreTodosLosUsuarios() throws Exception {
        when(uS.buscarPorNombreODni("rojas", null)).thenReturn(List.of(luis));

        mockMvc.perform(get("/api/usuarios/Buscar").param("texto", "rojas")
                        .with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("luis@clinica.pe"));

        verify(uS).buscarPorNombreODni("rojas", null);
    }

    @Test
    void buscar_textoVacio_400() throws Exception {
        mockMvc.perform(get("/api/usuarios/Buscar").param("texto", "  ")
                        .with(token("admin@clinica.pe", "ADMIN")))
                .andExpect(status().isBadRequest());

        verify(uS, never()).buscarPorNombreODni(anyString(), any());
    }

    // ---- Perfil del usuario logueado ----

    @Test
    void perfil_devuelveElUsuarioDelToken() throws Exception {
        when(uS.listByCorreo("luis@clinica.pe")).thenReturn(Optional.of(luis));

        mockMvc.perform(get("/api/usuarios/perfil").with(token("luis@clinica.pe", "MEDICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(2))
                .andExpect(jsonPath("$.nombres").value("Luis"));
    }

    @Test
    void perfil_sinToken_401() throws Exception {
        mockMvc.perform(get("/api/usuarios/perfil"))
                .andExpect(status().isUnauthorized());
    }

    // ---- Registro publico (permitAll) ----

    @Test
    void registrar_sinToken_creaUnPaciente_201() throws Exception {
        when(rS.listByNombre("PACIENTE")).thenReturn(Optional.of(new Roles(3, "PACIENTE")));

        mockMvc.perform(post("/api/usuarios/Registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombres": "Rosa", "apellidos": "Diaz", "dni": "44556677",
                                 "telefono": "955555555", "correo": "rosa@mail.com", "contrasenaHash": "123456"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRol").value(3));

        verify(uS).insert(any());
    }
}
