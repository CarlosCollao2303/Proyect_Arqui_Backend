package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;

import java.time.LocalDate;
import java.util.List;

public interface ITratamientosRepository extends JpaRepository<Tratamientos, Integer> {
    List<Tratamientos> findByEstadoIgnoreCase(String estado);

    List<Tratamientos> findByFechaInicio(LocalDate fechaInicio);

    // Tratamientos vigentes en una fecha: ya empezaron y todavia no terminan, del que termina antes al que termina despues.
    @Query("SELECT t FROM Tratamientos t " +
            "WHERE t.fechaInicio <= :fecha AND t.fechaFin >= :fecha " +
            "ORDER BY t.fechaFin ASC")
    List<Tratamientos> listarTratamientosVigentes(@Param("fecha") LocalDate fecha);
}
