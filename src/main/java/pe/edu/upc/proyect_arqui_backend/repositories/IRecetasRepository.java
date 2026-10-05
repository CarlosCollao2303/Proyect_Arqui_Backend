package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.Recetas;

import java.util.List;
import java.time.LocalDateTime;

public interface IRecetasRepository extends JpaRepository<Recetas, Integer> {
    List<Recetas> findByMedicamentoContainingIgnoreCase(String medicamento);

    List<Recetas> findByFechaEmisionGreaterThanEqualAndFechaEmisionLessThan(
            LocalDateTime inicio, LocalDateTime fin);
}
