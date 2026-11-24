package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.proyect_arqui_backend.dtos.HistorialClinicoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Diagnostico;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;
import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;
import pe.edu.upc.proyect_arqui_backend.entities.Recetas;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.repositories.ICitasRepository;
import pe.edu.upc.proyect_arqui_backend.repositories.IDetalleHistorialRepository;
import pe.edu.upc.proyect_arqui_backend.repositories.IHistoriasClinicasRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoriasClinicasServiceImplementTest {

    @Mock
    private IHistoriasClinicasRepository hR;

    @Mock
    private IDetalleHistorialRepository dR;

    @Mock
    private ICitasRepository cR;

    @InjectMocks
    private HistoriasClinicasServiceImplement service;

    private final Usuarios ana = new Usuarios(1, new Roles(3, "PACIENTE"), null, "Ana", "Torres", "12345678",
            "999999999", "ana@mail.com", "hash", null, null, true);
    private final Usuarios luis = new Usuarios(2, new Roles(2, "MEDICO"), null, "Luis", "Rojas", "87654321",
            "988888888", "luis@clinica.pe", "hash", null, null, true);
    private final HistoriasClinicas historia = new HistoriasClinicas(5, ana, LocalDateTime.of(2026, 1, 10, 0, 0));

    @BeforeEach
    void setUp() {
        Examenes hemograma = new Examenes(1, "Hemograma", "Normal",
                LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 2, 3, 0, 0));
        Diagnostico gripe = new Diagnostico(1, "J06.9", "Infeccion respiratoria",
                LocalDateTime.of(2026, 2, 1, 0, 0));
        Recetas paracetamol = new Recetas(1, "Paracetamol", "500 mg", "Cada 8 horas", 5, null,
                LocalDateTime.of(2026, 2, 1, 0, 0));

        // Dos consultas que comparten el mismo examen: en el historial debe salir una sola vez.
        DetalleHistorial primera = new DetalleHistorial(1, historia, luis, "Fiebre", null,
                LocalDateTime.of(2026, 2, 1, 0, 0));
        primera.setExamen(hemograma);
        primera.setDiagnostico(gripe);
        primera.setReceta(paracetamol);
        DetalleHistorial control = new DetalleHistorial(2, historia, luis, "Control", null,
                LocalDateTime.of(2026, 2, 10, 0, 0));
        control.setExamen(hemograma);

        when(dR.findAllByHistoriaClinica_IdHistoriaClinicaOrderByFechaRegistroDesc(5))
                .thenReturn(List.of(control, primera));
        when(cR.findEstadosDelPaciente(1)).thenReturn(List.of());
    }

    @Test
    void armarHistorial_vistaPaciente_soloCitasExamenesYRecetas() {
        HistorialClinicoDTO dto = service.armarHistorial(historia, true);

        assertEquals("Ana Torres", dto.getPaciente());
        assertEquals(0, dto.getCitas().size());
        assertEquals(1, dto.getExamenes().size());
        assertEquals(1, dto.getRecetas().size());
        assertNull(dto.getDiagnosticos());
        assertNull(dto.getTratamientos());
        assertNull(dto.getConsultas());
    }

    @Test
    void armarHistorial_vistaCompleta_traeTodoYSinRepetirElExamen() {
        HistorialClinicoDTO dto = service.armarHistorial(historia, false);

        assertEquals(1, dto.getExamenes().size());
        assertEquals("Hemograma", dto.getExamenes().get(0).getTipoExamen());
        assertEquals(1, dto.getDiagnosticos().size());
        assertEquals(0, dto.getTratamientos().size());
        assertEquals(2, dto.getConsultas().size());
        assertEquals("Luis Rojas", dto.getConsultas().get(0).medico());
    }
}
