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
