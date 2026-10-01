package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.repositories.ICitasRepository;
import pe.edu.upc.proyect_arqui_backend.repositories.IUsuariosRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitasServiceImplementTest {

    @Mock
    private ICitasRepository cR;

    @Mock
    private IUsuariosRepository uR;

    @InjectMocks
    private CitasServiceImplement cS;

    private final CitaEstadoDTO estado = new CitaEstadoDTO(
            1, LocalDateTime.of(2026, 10, 5, 9, 30), "PENDIENTE",
            "Ana Torres", "Luis Rojas", "Cardiologia"
    );

    @Test
    void consultarEstado_staffVeCualquierCitaSinRevisarDueno() {
        when(cR.findEstadoById(1)).thenReturn(Optional.of(estado));

        CitaEstadoDTO resultado = cS.consultarEstado(1, "medico@clinica.pe", true);

        assertEquals(estado, resultado);
        verify(cR, never()).existsByIdCitaAndPacienteCorreo(anyInt(), anyString());
    }

    @Test
    void consultarEstado_pacienteVeSuPropiaCita() {
        when(cR.findEstadoById(1)).thenReturn(Optional.of(estado));
        when(cR.existsByIdCitaAndPacienteCorreo(1, "ana@mail.com")).thenReturn(true);

        assertEquals(estado, cS.consultarEstado(1, "ana@mail.com", false));
    }

    @Test
    void consultarEstado_pacienteNoPuedeVerCitaAjena() {
        when(cR.findEstadoById(1)).thenReturn(Optional.of(estado));
        when(cR.existsByIdCitaAndPacienteCorreo(1, "otro@mail.com")).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> cS.consultarEstado(1, "otro@mail.com", false));
    }

    @Test
    void consultarEstado_citaInexistenteLanza404() {
        when(cR.findEstadoById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> cS.consultarEstado(99, "admin@clinica.pe", true));

        assertEquals("La cita con id 99 no existe", ex.getMessage());
    }

    private final CitaMedicoDTO citaMedico = new CitaMedicoDTO(
            1, LocalDateTime.of(2026, 10, 5, 9, 30), "PENDIENTE", "Ana Torres", "Cardiologia"
    );

    private Usuarios medicoConId(int id) {
        Usuarios medico = new Usuarios();
        medico.setIdUsuario(id);
        return medico;
    }

    @Test
    void listarCitasDelMedico_sinEstadoUsaPendienteYFiltraPorElIdDelToken() {
        when(uR.findByCorreo("luis@clinica.pe")).thenReturn(Optional.of(medicoConId(7)));
        when(cR.findCitasDelMedico(7, "PENDIENTE", null, null)).thenReturn(List.of(citaMedico));

        List<CitaMedicoDTO> resultado = cS.listarCitasDelMedico("luis@clinica.pe", null, null, null);

        assertEquals(List.of(citaMedico), resultado);
    }

    @Test
    void listarCitasDelMedico_normalizaEstadoYConvierteElRangoDeDias() {
        when(uR.findByCorreo("luis@clinica.pe")).thenReturn(Optional.of(medicoConId(7)));
        when(cR.findCitasDelMedico(7, "ATENDIDA",
                LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 11, 1, 0, 0)))
                .thenReturn(List.of(citaMedico));

        cS.listarCitasDelMedico("luis@clinica.pe", " atendida ",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        verify(cR).findCitasDelMedico(7, "ATENDIDA",
                LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 11, 1, 0, 0));
    }

    @Test
    void listarCitasDelMedico_sinCitasLanza404() {
        when(uR.findByCorreo("luis@clinica.pe")).thenReturn(Optional.of(medicoConId(7)));
        when(cR.findCitasDelMedico(7, "PENDIENTE", null, null)).thenReturn(List.of());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> cS.listarCitasDelMedico("luis@clinica.pe", null, null, null));

        assertEquals("No tienes citas programadas", ex.getMessage());
    }

    @Test
    void listarCitasDelMedico_inicioPosteriorAFinLanza400() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cS.listarCitasDelMedico("luis@clinica.pe", null,
                        LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1)));

        assertEquals("La fecha de inicio no puede ser posterior a la fecha de fin", ex.getMessage());
        verify(cR, never()).findCitasDelMedico(anyInt(), any(), any(), any());
    }

    @Test
    void listarCitasDelMedico_estadoInvalidoLanza400() {
        when(uR.findByCorreo("luis@clinica.pe")).thenReturn(Optional.of(medicoConId(7)));

        assertThrows(BadRequestException.class,
                () -> cS.listarCitasDelMedico("luis@clinica.pe", "EN_CURSO", null, null));
        verify(cR, never()).findCitasDelMedico(anyInt(), any(), any(), any());
    }
}
