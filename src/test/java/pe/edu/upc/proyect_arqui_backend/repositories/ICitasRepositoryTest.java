package pe.edu.upc.proyect_arqui_backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Ejecuta la JPQL real contra H2 en memoria (no toca la BD de Render).
@DataJpaTest(properties = "spring.jpa.database=h2")
class ICitasRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ICitasRepository cR;

    private Roles rolMedico;
    private Usuarios paciente;

    @BeforeEach
    void setUp() {
        rolMedico = em.persist(new Roles(0, "MEDICO"));
        Roles rolPaciente = em.persist(new Roles(0, "PACIENTE"));
        paciente = em.persist(usuario(rolPaciente, null, "Ana", "Torres", "ana@mail.com"));
    }

    @Test
    void findEstadoById_devuelveDatosConJoins() {
        Especialidades cardio = new Especialidades();
        cardio.setNombre("Cardiologia");
        em.persist(cardio);
        Usuarios medico = em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe"));
        Citas cita = em.persist(new Citas(0, paciente, medico,
                LocalDateTime.of(2026, 10, 5, 9, 30), 0, "PENDIENTE"));
        em.flush();

        CitaEstadoDTO dto = cR.findEstadoById(cita.getIdCita()).orElseThrow();

        assertEquals(cita.getIdCita(), dto.idCita());
        assertEquals(LocalDate.of(2026, 10, 5), dto.fecha());
        assertEquals(LocalTime.of(9, 30), dto.hora());
        assertEquals("PENDIENTE", dto.estado());
        assertEquals("Ana Torres", dto.paciente());
        assertEquals("Luis Rojas", dto.medico());
        assertEquals("Cardiologia", dto.especialidad());
    }

    @Test
    void findEstadoById_medicoSinEspecialidadIgualDevuelveLaCita() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        Citas cita = em.persist(new Citas(0, paciente, medico,
                LocalDateTime.of(2026, 10, 5, 9, 30), 0, "ATENDIDA"));
        em.flush();

        CitaEstadoDTO dto = cR.findEstadoById(cita.getIdCita()).orElseThrow();

        assertEquals("ATENDIDA", dto.estado());
        assertNull(dto.especialidad());
    }

    @Test
    void findEstadoById_citaInexistente() {
        assertEquals(Optional.empty(), cR.findEstadoById(12345));
    }

    @Test
    void existsByIdCitaAndPacienteCorreo_soloParaElPacienteDeLaCita() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        Citas cita = em.persist(new Citas(0, paciente, medico,
                LocalDateTime.of(2026, 10, 5, 9, 30), 0, "PENDIENTE"));
        em.flush();

        assertTrue(cR.existsByIdCitaAndPacienteCorreo(cita.getIdCita(), "ana@mail.com"));
        assertFalse(cR.existsByIdCitaAndPacienteCorreo(cita.getIdCita(), "luis@clinica.pe"));
    }

    private Usuarios usuario(Roles rol, Especialidades especialidad, String nombres,
                             String apellidos, String correo) {
        return new Usuarios(0, rol, especialidad, nombres, apellidos, "12345678",
                "999999999", correo, "hash", null, null, true);
    }
}
