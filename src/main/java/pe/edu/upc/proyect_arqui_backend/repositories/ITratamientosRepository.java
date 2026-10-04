package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;

import java.time.LocalDate;
import java.util.List;

public interface ITratamientosRepository extends JpaRepository<Tratamientos, Integer> {
    List<Tratamientos> findByEstadoIgnoreCase(String estado);

    List<Tratamientos> findByFechaInicioBetween(LocalDate inicio, LocalDate fin);
}
