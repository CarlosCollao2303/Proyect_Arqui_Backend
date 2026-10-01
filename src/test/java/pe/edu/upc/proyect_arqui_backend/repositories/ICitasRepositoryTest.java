package pe.edu.upc.proyect_arqui_backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void findCitasDelMedico_soloSusCitasConPacienteYEspecialidadOrdenadas() {
        Especialidades cardio = new Especialidades();
        cardio.setNombre("Cardiologia");
        em.persist(cardio);
        Usuarios medico = em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe"));
        Usuarios otroMedico = em.persist(usuario(rolMedico, null, "Eva", "Diaz", "eva@clinica.pe"));
        Citas tarde = em.persist(new Citas(0, paciente, medico,
                LocalDateTime.of(2026, 10, 5, 15, 0), 0, "PENDIENTE"));
        Citas temprano = em.persist(new Citas(0, paciente, medico,
                LocalDateTime.of(2026, 10, 5, 9, 30), 0, "PENDIENTE"));
        em.persist(new Citas(0, paciente, otroMedico,
                LocalDateTime.of(2026, 10, 5, 8, 0), 0, "PENDIENTE"));
        em.flush();

        List<CitaMedicoDTO> citas = cR.findCitasDelMedico(medico.getIdUsuario(), "PENDIENTE", null, null);

        assertEquals(2, citas.size());
        assertEquals(temprano.getIdCita(), citas.get(0).idCita());
        assertEquals(tarde.getIdCita(), citas.get(1).idCita());
        CitaMedicoDTO primera = citas.get(0);
        assertEquals(LocalDate.of(2026, 10, 5), primera.fecha());
        assertEquals(LocalTime.of(9, 30), primera.hora());
        assertEquals("PENDIENTE", primera.estado());
        assertEquals("Ana Torres", primera.paciente());
        assertEquals("Cardiologia", primera.especialidad());
    }

    @Test
    void findCitasDelMedico_filtraPorEstadoYNullTraeTodos() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        em.persist(new Citas(0, paciente, medico, LocalDateTime.of(2026, 10, 5, 9, 0), 0, "PENDIENTE"));
        em.persist(new Citas(0, paciente, medico, LocalDateTime.of(2026, 10, 6, 9, 0), 0, "ATENDIDA"));
        em.persist(new Citas(0, paciente, medico, LocalDateTime.of(2026, 10, 7, 9, 0), 0, "CANCELADA"));
        em.flush();

        List<CitaMedicoDTO> atendidas = cR.findCitasDelMedico(medico.getIdUsuario(), "ATENDIDA", null, null);
        assertEquals(1, atendidas.size());
        assertEquals("ATENDIDA", atendidas.get(0).estado());
        assertNull(atendidas.get(0).especialidad());

        assertEquals(3, cR.findCitasDelMedico(medico.getIdUsuario(), null, null, null).size());
    }

    @Test
    void findCitasDelMedico_filtraPorRangoDeFechas() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        em.persist(new Citas(0, paciente, medico, LocalDateTime.of(2026, 10, 1, 8, 0), 0, "PENDIENTE"));
        em.persist(new Citas(0, paciente, medico, LocalDateTime.of(2026, 10, 10, 23, 30), 0, "PENDIENTE"));
        em.persist(new Citas(0, paciente, medico, LocalDateTime.of(2026, 10, 11, 0, 0), 0, "PENDIENTE"));
        em.flush();

        List<CitaMedicoDTO> citas = cR.findCitasDelMedico(medico.getIdUsuario(), null,
                LocalDateTime.of(2026, 10, 2, 0, 0), LocalDateTime.of(2026, 10, 11, 0, 0));

        assertEquals(1, citas.size());
        assertEquals(LocalDate.of(2026, 10, 10), citas.get(0).fecha());

        assertEquals(2, cR.findCitasDelMedico(medico.getIdUsuario(), null,
                LocalDateTime.of(2026, 10, 10, 0, 0), null).size());
    }

    @Test
    void findCitasDelMedico_sinCitasDevuelveListaVacia() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        em.flush();

        assertTrue(cR.findCitasDelMedico(medico.getIdUsuario(), "PENDIENTE", null, null).isEmpty());
    }

    private Usuarios usuario(Roles rol, Especialidades especialidad, String nombres,
                             String apellidos, String correo) {
        return new Usuarios(0, rol, especialidad, nombres, apellidos, "12345678",
                "999999999", correo, "hash", null, null, true);
    }
}
