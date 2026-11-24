package pe.edu.upc.proyect_arqui_backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = "spring.jpa.database=h2")
class IUsuariosRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private IUsuariosRepository uR;

    private Usuarios luis;
    private Usuarios eva;
    private Usuarios ana;
    private Usuarios bruno;
    private Usuarios carla;

    @BeforeEach
    void setUp() {
        Roles rolMedico = em.persist(new Roles(0, "MEDICO"));
        Roles rolPaciente = em.persist(new Roles(0, "PACIENTE"));

        luis = em.persist(usuario(rolMedico, "Luis", "Rojas", "11111111", "luis@clinica.pe"));
        eva = em.persist(usuario(rolMedico, "Eva", "Diaz", "22222222", "eva@clinica.pe"));
        ana = em.persist(usuario(rolPaciente, "Ana", "Torres", "33333333", "ana@mail.com"));
        bruno = em.persist(usuario(rolPaciente, "Bruno", "Alva", "44444444", "bruno@mail.com"));
        carla = em.persist(usuario(rolPaciente, "Carla", "Mena", "55555555", "carla@mail.com"));
    }

    @Test
    void findPacientesDelMedico_sinRepetirYOrdenadosPorApellido() {
        // Ana tiene dos citas con Luis: debe salir una sola vez
        cita(ana, luis);
        cita(ana, luis);
        cita(bruno, luis);
        // Carla es paciente de otro medico
        cita(carla, eva);
        em.flush();

        List<Usuarios> pacientes = uR.findPacientesDelMedico("luis@clinica.pe");

        assertEquals(2, pacientes.size());
        assertEquals("Alva", pacientes.get(0).getApellidos());
        assertEquals("Torres", pacientes.get(1).getApellidos());
    }

    @Test
    void findPacientesDelMedico_medicoSinCitas_listaVacia() {
        em.flush();

        assertEquals(0, uR.findPacientesDelMedico("eva@clinica.pe").size());
    }

    @Test
    void buscarPorNombreODni_porNombreCompletoSinImportarMayusculas() {
        em.flush();

        List<Usuarios> encontrados = uR.buscarPorNombreODni("ana TOR", null);

        assertEquals(1, encontrados.size());
        assertEquals("ana@mail.com", encontrados.get(0).getCorreo());
    }

    @Test
    void buscarPorNombreODni_sinImportarLasTildes() {
        em.persist(usuario(ana.getRol(), "Juan", "Pérez García", "66666666", "juan@mail.com"));
        em.flush();

        // Se escribe sin tildes y encuentra "Perez Garcia" con tildes, y al reves
        assertEquals(1, uR.buscarPorNombreODni("juan perez", null).size());
        assertEquals(1, uR.buscarPorNombreODni("García", null).size());
    }

    @Test
    void buscarPorNombreODni_porDni() {
        em.flush();

        List<Usuarios> encontrados = uR.buscarPorNombreODni("4444", null);

        assertEquals(1, encontrados.size());
        assertEquals("Bruno", encontrados.get(0).getNombres());
    }

    @Test
    void buscarPorNombreODni_conRol_soloEseRol() {
        em.flush();

        // "a" aparece en casi todos; con rol PACIENTE no deben salir los medicos
        List<Usuarios> pacientes = uR.buscarPorNombreODni("a", "PACIENTE");

        assertEquals(3, pacientes.size());
        pacientes.forEach(u -> assertEquals("PACIENTE", u.getRol().getNombre()));
    }

    @Test
    void findByRol_ordenadosPorApellido() {
        em.flush();

        List<Usuarios> medicos = uR.findByRol_NombreOrderByApellidosAscNombresAsc("MEDICO");

        assertEquals(2, medicos.size());
        assertEquals("Diaz", medicos.get(0).getApellidos());
    }

    private void cita(Usuarios paciente, Usuarios medico) {
        em.persist(new Citas(0, paciente, medico, LocalDateTime.of(2026, 10, 5, 9, 30), 0, "PENDIENTE"));
    }

    private Usuarios usuario(Roles rol, String nombres, String apellidos, String dni, String correo) {
        return new Usuarios(0, rol, null, nombres, apellidos, dni, "999999999", correo, "hash", null, null, true);
    }
}
