package pe.edu.upc.proyect_arqui_backend.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.edu.upc.proyect_arqui_backend.dtos.ConteoNotificacionesEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.DiagnosticoFrecuenteDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Diagnostico;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = "spring.jpa.database=h2")
class ConsultasPersonalizadasRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private IDiagnosticoRepository diagnosticoRepository;

    @Autowired
    private INotificacionesRepository notificacionesRepository;

    @Test
    void listarDiagnosticosFrecuentes_retornaAgrupadosYOrdenadosPorFrecuencia() {
        Diagnostico d1 = new Diagnostico(0, "J06.9", "Infeccion respiratoria", LocalDateTime.now());
        Diagnostico d2 = new Diagnostico(0, "J06.9", "Infeccion respiratoria", LocalDateTime.now());
        Diagnostico d3 = new Diagnostico(0, "I10", "Hipertension", LocalDateTime.now());

        em.persist(d1);
        em.persist(d2);
        em.persist(d3);
        em.flush();

        List<DiagnosticoFrecuenteDTO> resultado = diagnosticoRepository.listarDiagnosticosFrecuentes();

        assertEquals(2, resultado.size());
        assertEquals("J06.9", resultado.get(0).getCodigoCie10());
        assertEquals(2L, resultado.get(0).getCantidad());
        assertEquals("I10", resultado.get(1).getCodigoCie10());
        assertEquals(1L, resultado.get(1).getCantidad());
    }

    @Test
    void contarNotificacionesPorEstado_retornaConteoAgrupado() {
        Roles rol = em.persist(new Roles(0, "PACIENTE"));
        Usuarios usuario = em.persist(new Usuarios(0, rol, null, "Carlos", "Mendoza", "12345678",
                "999888777", "carlos@test.com", "hash", null, null, true));

        Notificaciones n1 = new Notificaciones(0, usuario, "RECORDATORIO", "Msg 1", LocalDateTime.now(), "ENVIADO");
        Notificaciones n2 = new Notificaciones(0, usuario, "AVISO_GENERAL", "Msg 2", LocalDateTime.now(), "ENVIADO");
        Notificaciones n3 = new Notificaciones(0, usuario, "CITA_REGISTRADA", "Msg 3", LocalDateTime.now(), "FALLIDO");

        em.persist(n1);
        em.persist(n2);
        em.persist(n3);
        em.flush();

        List<ConteoNotificacionesEstadoDTO> resultado = notificacionesRepository.contarNotificacionesPorEstado();

        assertEquals(2, resultado.size());
        assertEquals("ENVIADO", resultado.get(0).getEstadoEnvio());
        assertEquals(2L, resultado.get(0).getTotal());
        assertEquals("FALLIDO", resultado.get(1).getEstadoEnvio());
        assertEquals(1L, resultado.get(1).getTotal());
    }
}
