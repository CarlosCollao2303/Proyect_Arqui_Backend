package pe.edu.upc.proyect_arqui_backend.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.edu.upc.proyect_arqui_backend.dtos.TratamientoPacienteDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.database=h2")
class ITratamientosRepositoryTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 4);

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ITratamientosRepository tR;

    @Test
    void listarTratamientosVigentes_soloLosQueCubrenLaFechaOrdenadosPorFechaFin() {
        Tratamientos terminaDespues = em.persist(tratamiento("Fisioterapia", HOY.minusDays(10), HOY.plusDays(20)));
        Tratamientos terminaHoy = em.persist(tratamiento("Antibiotico", HOY.minusDays(7), HOY));
        Tratamientos empiezaHoy = em.persist(tratamiento("Dieta", HOY, HOY.plusDays(5)));
        em.persist(tratamiento("Ya termino", HOY.minusDays(30), HOY.minusDays(1)));
        em.persist(tratamiento("Aun no empieza", HOY.plusDays(1), HOY.plusDays(10)));
        em.flush();

        List<Tratamientos> vigentes = tR.listarTratamientosVigentes(HOY);

        assertEquals(3, vigentes.size());
        assertEquals(terminaHoy.getIdTratamiento(), vigentes.get(0).getIdTratamiento());
        assertEquals(empiezaHoy.getIdTratamiento(), vigentes.get(1).getIdTratamiento());
        assertEquals(terminaDespues.getIdTratamiento(), vigentes.get(2).getIdTratamiento());
    }

    @Test
    void listarTratamientosDePaciente_traeMedicoYMotivoSoloDeEsePaciente() {
        Roles rolMedico = em.persist(new Roles(0, "MEDICO"));
        Roles rolPaciente = em.persist(new Roles(0, "PACIENTE"));
        Usuarios medico = em.persist(usuario(rolMedico, "Luis", "Rojas", "luis@clinica.pe"));
        Usuarios ana = em.persist(usuario(rolPaciente, "Ana", "Torres", "ana@mail.com"));
        Usuarios otro = em.persist(usuario(rolPaciente, "Pedro", "Diaz", "pedro@mail.com"));
        HistoriasClinicas historiaAna = em.persist(new HistoriasClinicas(0, ana, LocalDateTime.of(2026, 1, 1, 8, 0)));
        HistoriasClinicas historiaOtro = em.persist(new HistoriasClinicas(0, otro, LocalDateTime.of(2026, 1, 1, 8, 0)));

        Tratamientos antiguo = em.persist(tratamiento("Reposo", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 10)));
        Tratamientos reciente = em.persist(tratamiento("Fisioterapia", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
        Tratamientos deOtro = em.persist(tratamiento("Dieta", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 30)));
        em.persist(detalle(historiaAna, medico, "Dolor de espalda", antiguo));
        em.persist(detalle(historiaAna, medico, "Lesion de rodilla", reciente));
        em.persist(detalle(historiaAna, medico, "Control sin tratamiento", null));
        em.persist(detalle(historiaOtro, medico, "Sobrepeso", deOtro));
        em.flush();

        List<TratamientoPacienteDTO> lista = tR.listarTratamientosDePaciente(ana.getIdUsuario());

        assertEquals(2, lista.size());
        TratamientoPacienteDTO primero = lista.get(0);
        assertEquals(reciente.getIdTratamiento(), primero.idTratamiento());
        assertEquals("Fisioterapia", primero.descripcion());
        assertEquals(LocalDate.of(2026, 9, 1), primero.fechaInicio());
        assertEquals(LocalDate.of(2026, 9, 30), primero.fechaFin());
        assertEquals("ACTIVO", primero.estado());
        assertEquals("Luis Rojas", primero.medico());
        assertEquals("Lesion de rodilla", primero.motivoConsulta());
        assertEquals(antiguo.getIdTratamiento(), lista.get(1).idTratamiento());
    }

    @Test
    void listarTratamientosDePaciente_sinTratamientosDevuelveListaVacia() {
        assertTrue(tR.listarTratamientosDePaciente(12345).isEmpty());
    }

    private Tratamientos tratamiento(String descripcion, LocalDate inicio, LocalDate fin) {
        return new Tratamientos(0, descripcion, inicio, fin, "ACTIVO");
    }

    private DetalleHistorial detalle(HistoriasClinicas historia, Usuarios medico, String motivo,
                                     Tratamientos tratamiento) {
        DetalleHistorial detalle = new DetalleHistorial(0, historia, medico, motivo, null,
                LocalDateTime.of(2026, 10, 1, 10, 0));
        detalle.setTratamiento(tratamiento);
        return detalle;
    }

    private Usuarios usuario(Roles rol, String nombres, String apellidos, String correo) {
        return new Usuarios(0, rol, null, nombres, apellidos, "12345678",
                "999999999", correo, "hash", null, null, true);
    }
}
