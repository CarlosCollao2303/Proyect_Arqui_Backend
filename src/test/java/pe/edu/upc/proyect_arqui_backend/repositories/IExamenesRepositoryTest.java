package pe.edu.upc.proyect_arqui_backend.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.database=h2")
class IExamenesRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private IExamenesRepository eR;

    @Test
    void obtenerTiempoEntregaPorTipo_promediaSoloConResultadoYOrdenaPorDias() {
        // Hemograma: 1 y 2 dias -> promedio 1.5, maximo 2; mas uno pendiente
        examen("Hemograma", LocalDateTime.of(2026, 9, 10, 0, 0), LocalDateTime.of(2026, 9, 11, 0, 0));
        examen("Hemograma", LocalDateTime.of(2026, 9, 15, 0, 0), LocalDateTime.of(2026, 9, 17, 0, 0));
        examen("Hemograma", LocalDateTime.of(2026, 11, 20, 0, 0), null);
        // Ecografia: 12 horas -> 0.5 dias (conserva el decimal)
        examen("Ecografia", LocalDateTime.of(2026, 10, 1, 8, 0), LocalDateTime.of(2026, 10, 1, 20, 0));
        // Resonancia: 4 dias, el mas lento
        examen("Resonancia", LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 5, 0, 0));
        // Urocultivo: ninguno con resultado, va al final
        examen("Urocultivo", LocalDateTime.of(2026, 11, 13, 0, 0), null);
        em.flush();

        List<Object[]> filas = eR.obtenerTiempoEntregaPorTipo();

        assertEquals(4, filas.size());
        assertFila(filas.get(0), "Resonancia", 1, 1, 0, 4.0, 4.0);
        assertFila(filas.get(1), "Hemograma", 3, 2, 1, 1.5, 2.0);
        assertFila(filas.get(2), "Ecografia", 1, 1, 0, 0.5, 0.5);

        assertEquals("Urocultivo", filas.get(3)[0]);
        assertEquals(1L, ((Number) filas.get(3)[3]).longValue());
        assertNull(filas.get(3)[4]);
        assertNull(filas.get(3)[5]);
    }

    @Test
    void obtenerTiempoEntregaPorTipo_sinExamenesDevuelveListaVacia() {
        assertTrue(eR.obtenerTiempoEntregaPorTipo().isEmpty());
    }

    private void examen(String tipo, LocalDateTime solicitud, LocalDateTime resultado) {
        em.persist(new Examenes(0, tipo, null, solicitud, resultado));
    }

    private void assertFila(Object[] fila, String tipo, long solicitados, long conResultado,
                            long pendientes, double diasPromedio, double diasMaximo) {
        assertEquals(tipo, fila[0]);
        assertEquals(solicitados, ((Number) fila[1]).longValue());
        assertEquals(conResultado, ((Number) fila[2]).longValue());
        assertEquals(pendientes, ((Number) fila[3]).longValue());
        assertEquals(diasPromedio, ((Number) fila[4]).doubleValue());
        assertEquals(diasMaximo, ((Number) fila[5]).doubleValue());
    }
}
