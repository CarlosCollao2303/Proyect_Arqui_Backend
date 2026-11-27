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

    @Test
    void findTodasLasCitasEntre_filtraDiaYDevuelvePacienteYMedicoOrdenados() {
        Especialidades cardio = new Especialidades();
        cardio.setNombre("Cardiologia");
        em.persist(cardio);
        Usuarios medico = em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe"));
        Citas tarde = em.persist(new Citas(0, paciente, medico,
                LocalDateTime.of(2026, 10, 5, 15, 0), 0, "PENDIENTE"));
        Citas temprano = em.persist(new Citas(0, paciente, medico,
                LocalDateTime.of(2026, 10, 5, 9, 30), 0, "ATENDIDA"));
        em.persist(new Citas(0, paciente, medico,
                LocalDateTime.of(2026, 10, 6, 0, 0), 0, "PENDIENTE"));
        em.flush();

        List<pe.edu.upc.proyect_arqui_backend.dtos.CitaPacienteDTO> citas =
                cR.findTodasLasCitasEntre(LocalDate.of(2026, 10, 5).atStartOfDay(),
                        LocalDate.of(2026, 10, 6).atStartOfDay());

        assertEquals(2, citas.size());
        assertEquals(temprano.getIdCita(), citas.get(0).getIdCita());
        assertEquals(tarde.getIdCita(), citas.get(1).getIdCita());
        assertEquals("Ana Torres", citas.get(0).getPaciente());
        assertEquals("Luis Rojas", citas.get(0).getMedico());
        assertEquals("Cardiologia", citas.get(0).getEspecialidad());
    }

    @Test
    void contarCitasPorEspecialidadYEstado_separaPorEstadoYOrdenaPorTotal() {
        Especialidades cardio = especialidad("Cardiologia");
        Especialidades pediatria = especialidad("Pediatria");
        Usuarios cardiologo = em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe"));
        Usuarios pediatra = em.persist(usuario(rolMedico, pediatria, "Rosa", "Diaz", "rosa@clinica.pe"));
        cita(cardiologo, "ATENDIDA");
        cita(cardiologo, "ATENDIDA");
        cita(cardiologo, "PENDIENTE");
        cita(cardiologo, "CANCELADA");
        cita(pediatra, "PENDIENTE");
        em.flush();

        List<Object[]> filas = cR.contarCitasPorEspecialidadYEstado();

        assertEquals(2, filas.size());
        // Primero la especialidad con mas citas
        assertFila(filas.get(0), "Cardiologia", 2, 1, 1, 4);
        assertFila(filas.get(1), "Pediatria", 0, 1, 0, 1);
    }

    @Test
    void contarCitasPorEspecialidadYEstado_medicoSinEspecialidadSeAgrupaAparte() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        cita(medico, "CANCELADA");
        em.flush();

        List<Object[]> filas = cR.contarCitasPorEspecialidadYEstado();

        assertEquals(1, filas.size());
        assertFila(filas.get(0), "Sin especialidad", 0, 0, 1, 1);
    }

    @Test
    void contarCitasPorEspecialidadYEstado_sinCitasDevuelveListaVacia() {
        assertTrue(cR.contarCitasPorEspecialidadYEstado().isEmpty());
    }

    @Test
    void obtenerEsperaPromedioPorMedico_soloCuentaAtendidasYOrdenaPorEspera() {
        Especialidades cardio = especialidad("Cardiologia");
        Usuarios cardiologo = em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe"));
        Usuarios general = em.persist(usuario(rolMedico, null, "Rosa", "Diaz", "rosa@clinica.pe"));
        cita(cardiologo, "ATENDIDA", 10);
        cita(cardiologo, "ATENDIDA", 15);
        // Las pendientes y canceladas no deben mover el promedio
        cita(cardiologo, "PENDIENTE", 99);
        cita(cardiologo, "CANCELADA", 99);
        cita(general, "ATENDIDA", 30);
        em.flush();

        List<Object[]> filas = cR.obtenerEsperaPromedioPorMedico();

        assertEquals(2, filas.size());
        // Primero el medico con mas espera
        assertEquals("Rosa Diaz", filas.get(0)[0]);
        assertEquals("Sin especialidad", filas.get(0)[1]);
        assertEquals(1L, ((Number) filas.get(0)[2]).longValue());
        assertEquals(30.0, ((Number) filas.get(0)[3]).doubleValue());

        assertEquals("Luis Rojas", filas.get(1)[0]);
        assertEquals("Cardiologia", filas.get(1)[1]);
        assertEquals(2L, ((Number) filas.get(1)[2]).longValue());
        // (10 + 15) / 2 = 12.5: el promedio conserva el decimal
        assertEquals(12.5, ((Number) filas.get(1)[3]).doubleValue());
        assertEquals(15, ((Number) filas.get(1)[4]).intValue());
    }

    @Test
    void obtenerEsperaPromedioPorMedico_medicoSinCitasAtendidasNoAparece() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        cita(medico, "PENDIENTE", 20);
        em.flush();

        assertTrue(cR.obtenerEsperaPromedioPorMedico().isEmpty());
    }

    @Test
    void contarCitasPorMesYEstado_agrupaPorMesSoloDelAnioPedido() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        citaEn(medico, LocalDateTime.of(2026, 3, 2, 9, 0), "ATENDIDA");
        citaEn(medico, LocalDateTime.of(2026, 3, 20, 9, 0), "CANCELADA");
        citaEn(medico, LocalDateTime.of(2026, 3, 31, 23, 30), "ATENDIDA");
        citaEn(medico, LocalDateTime.of(2026, 7, 1, 0, 0), "PENDIENTE");
        // Otro año: no debe sumarse
        citaEn(medico, LocalDateTime.of(2025, 3, 10, 9, 0), "ATENDIDA");
        em.flush();

        List<Object[]> filas = cR.contarCitasPorMesYEstado(2026);

        // Solo los meses con citas, en orden
        assertEquals(2, filas.size());
        assertMes(filas.get(0), 3, 2, 0, 1, 3);
        assertMes(filas.get(1), 7, 0, 1, 0, 1);
    }

    @Test
    void contarCitasPorMesYEstado_anioSinCitasDevuelveListaVacia() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        citaEn(medico, LocalDateTime.of(2026, 3, 2, 9, 0), "ATENDIDA");
        em.flush();

        assertTrue(cR.contarCitasPorMesYEstado(2024).isEmpty());
    }

    @Test
    void listarAniosConCitas_sinRepetirYDelMasReciente() {
        Usuarios medico = em.persist(usuario(rolMedico, null, "Luis", "Rojas", "luis@clinica.pe"));
        citaEn(medico, LocalDateTime.of(2025, 5, 2, 9, 0), "ATENDIDA");
        citaEn(medico, LocalDateTime.of(2026, 1, 2, 9, 0), "ATENDIDA");
        citaEn(medico, LocalDateTime.of(2026, 8, 2, 9, 0), "PENDIENTE");
        em.flush();

        assertEquals(List.of(2026, 2025), cR.listarAniosConCitas());
    }

    private void citaEn(Usuarios medico, LocalDateTime fecha, String estado) {
        em.persist(new Citas(0, paciente, medico, fecha, 0, estado));
    }

    private void assertMes(Object[] fila, int mes, long atendidas, long pendientes,
                           long canceladas, long total) {
        assertEquals(mes, ((Number) fila[0]).intValue());
        assertEquals(atendidas, ((Number) fila[1]).longValue());
        assertEquals(pendientes, ((Number) fila[2]).longValue());
        assertEquals(canceladas, ((Number) fila[3]).longValue());
        assertEquals(total, ((Number) fila[4]).longValue());
    }

    private Especialidades especialidad(String nombre) {
        Especialidades especialidad = new Especialidades();
        especialidad.setNombre(nombre);
        return em.persist(especialidad);
    }

    private void cita(Usuarios medico, String estado) {
        cita(medico, estado, 0);
    }

    private void cita(Usuarios medico, String estado, int minutosEspera) {
        em.persist(new Citas(0, paciente, medico, LocalDateTime.of(2026, 10, 5, 9, 30), minutosEspera, estado));
    }

    private void assertFila(Object[] fila, String especialidad, long atendidas, long pendientes,
                            long canceladas, long total) {
        assertEquals(especialidad, fila[0]);
        assertEquals(atendidas, ((Number) fila[1]).longValue());
        assertEquals(pendientes, ((Number) fila[2]).longValue());
        assertEquals(canceladas, ((Number) fila[3]).longValue());
        assertEquals(total, ((Number) fila[4]).longValue());
    }

    private Usuarios usuario(Roles rol, Especialidades especialidad, String nombres,
                             String apellidos, String correo) {
        return new Usuarios(0, rol, especialidad, nombres, apellidos, "12345678",
                "999999999", correo, "hash", null, null, true);
    }
}
