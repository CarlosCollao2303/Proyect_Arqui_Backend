package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.repositories.ICitasRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitasServiceImplementTest {

    @Mock
    private ICitasRepository cR;

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
}
