package pe.edu.upc.proyect_arqui_backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.edu.upc.proyect_arqui_backend.dtos.MedicoPorEspecialidadDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.database=h2")
class IEspecialidadesRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private IEspecialidadesRepository eR;

    private Roles rolMedico;
    private Roles rolPaciente;
    private Especialidades cardio;
    private Especialidades pediatria;

    @BeforeEach
    void setUp() {
        rolMedico = em.persist(new Roles(0, "MEDICO"));
        rolPaciente = em.persist(new Roles(0, "PACIENTE"));
        cardio = em.persist(especialidad("Cardiologia"));
        pediatria = em.persist(especialidad("Pediatria"));
    }

    @Test
    void findMedicosByEspecialidadId_soloMedicosActivosDeEsaEspecialidadOrdenados() {
        Usuarios rojas = em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe", true));
        Usuarios diazEva = em.persist(usuario(rolMedico, cardio, "Eva", "Diaz", "eva@clinica.pe", true));
        Usuarios diazAna = em.persist(usuario(rolMedico, cardio, "Ana", "Diaz", "ana.d@clinica.pe", true));
        em.persist(usuario(rolMedico, cardio, "Inactivo", "Perez", "perez@clinica.pe", false));
        em.persist(usuario(rolMedico, pediatria, "Otra", "Especialidad", "ped@clinica.pe", true));
        em.persist(usuario(rolPaciente, cardio, "No", "Medico", "pac@mail.com", true));
        em.flush();

        List<MedicoPorEspecialidadDTO> medicos = eR.findMedicosByEspecialidadId(cardio.getIdEspecialidad());

        assertEquals(3, medicos.size());
        assertEquals(diazAna.getIdUsuario(), medicos.get(0).idMedico());
        assertEquals(diazEva.getIdUsuario(), medicos.get(1).idMedico());
        assertEquals(rojas.getIdUsuario(), medicos.get(2).idMedico());
        MedicoPorEspecialidadDTO primero = medicos.get(0);
        assertEquals("Ana", primero.nombres());
        assertEquals("Diaz", primero.apellidos());
        assertEquals("Cardiologia", primero.especialidad());
    }

    @Test
    void findMedicosByEspecialidadId_especialidadSinMedicosDevuelveListaVacia() {
        em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe", true));
        em.flush();

        assertTrue(eR.findMedicosByEspecialidadId(pediatria.getIdEspecialidad()).isEmpty());
    }

    // ---- Reporte de medicos por especialidad ----

    @Test
    void contarMedicosPorEspecialidad_separaActivosEInactivosYSoloCuentaMedicos() {
        em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe", true));
        em.persist(usuario(rolMedico, cardio, "Eva", "Diaz", "eva@clinica.pe", true));
        em.persist(usuario(rolMedico, cardio, "Inactivo", "Perez", "perez@clinica.pe", false));
        em.persist(usuario(rolPaciente, cardio, "No", "Medico", "pac@mail.com", true));
        em.flush();

        List<Object[]> filas = eR.contarMedicosPorEspecialidad();

        Object[] cardiologia = fila(filas, "Cardiologia");
        assertEquals(cardio.getIdEspecialidad(), ((Number) cardiologia[0]).intValue());
        assertEquals(2L, ((Number) cardiologia[2]).longValue());
        assertEquals(1L, ((Number) cardiologia[3]).longValue());
        assertEquals(3L, ((Number) cardiologia[4]).longValue());
        // La especialidad con mas medicos activos va primero
        assertEquals("Cardiologia", filas.get(0)[1]);
    }

    @Test
    void contarMedicosPorEspecialidad_incluyeEspecialidadesSinMedicosEnCero() {
        em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe", true));
        em.flush();

        Object[] ped = fila(eR.contarMedicosPorEspecialidad(), "Pediatria");

        assertEquals(0L, ((Number) ped[2]).longValue());
        assertEquals(0L, ((Number) ped[3]).longValue());
        assertEquals(0L, ((Number) ped[4]).longValue());
    }

    @Test
    void contarMedicosPorEspecialidad_agrupaLosMedicosSinEspecialidad() {
        em.persist(usuario(rolMedico, null, "Sin", "Asignar", "sin@clinica.pe", true));
        em.persist(usuario(rolPaciente, null, "Otro", "Paciente", "otro@mail.com", true));
        em.flush();

        List<Object[]> filas = eR.contarMedicosPorEspecialidad();
        Object[] sinEspecialidad = fila(filas, "Sin especialidad");

        assertEquals(3, filas.size());
        assertEquals(null, sinEspecialidad[0]);
        assertEquals(1L, ((Number) sinEspecialidad[2]).longValue());
        assertEquals(1L, ((Number) sinEspecialidad[4]).longValue());
    }

    @Test
    void contarMedicosPorEspecialidad_sinMedicosSinEspecialidadNoAgregaEsaFila() {
        em.persist(usuario(rolMedico, cardio, "Luis", "Rojas", "luis@clinica.pe", true));
        em.flush();

        List<Object[]> filas = eR.contarMedicosPorEspecialidad();

        assertEquals(2, filas.size());
        assertTrue(filas.stream().noneMatch(f -> "Sin especialidad".equals(f[1])));
    }

    private Object[] fila(List<Object[]> filas, String especialidad) {
        return filas.stream()
                .filter(f -> especialidad.equals(f[1]))
                .findFirst()
                .orElseThrow();
    }

    private Especialidades especialidad(String nombre) {
        Especialidades e = new Especialidades();
        e.setNombre(nombre);
        return e;
    }

    private Usuarios usuario(Roles rol, Especialidades especialidad, String nombres,
                             String apellidos, String correo, boolean activo) {
        return new Usuarios(0, rol, especialidad, nombres, apellidos, "12345678",
                "999999999", correo, "hash", null, null, activo);
    }
}
