package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.Recetas;

import java.time.LocalDateTime;
import java.util.List;

public interface IRecetasRepository extends JpaRepository<Recetas, Integer> {
    List<Recetas> findByMedicamentoContainingIgnoreCase(String medicamento);

    List<Recetas> findByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);
}
