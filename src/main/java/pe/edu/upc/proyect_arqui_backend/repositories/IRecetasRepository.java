package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upc.proyect_arqui_backend.dtos.RecetaDetalleDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Recetas;

import java.util.List;
import java.time.LocalDateTime;

public interface IRecetasRepository extends JpaRepository<Recetas, Integer> {
    List<Recetas> findByMedicamentoContainingIgnoreCase(String medicamento);

    List<Recetas> findByFechaEmisionGreaterThanEqualAndFechaEmisionLessThan(
            LocalDateTime inicio, LocalDateTime fin);

    // Recetas con el paciente, el medico y el motivo de la consulta, de la mas reciente a la mas antigua.
    @Query("SELECT new pe.edu.upc.proyect_arqui_backend.dtos.RecetaDetalleDTO(" +
            "r.idReceta, " +
            "CONCAT(p.nombres, ' ', p.apellidos), " +
            "CONCAT(m.nombres, ' ', m.apellidos), " +
            "d.motivoConsulta, r.medicamento, r.dosis, r.frecuencia, r.duracionDias, r.fechaEmision) " +
            "FROM DetalleHistorial d " +
            "JOIN d.receta r " +
            "JOIN d.historiaClinica h " +
            "JOIN h.paciente p " +
            "JOIN d.medico m " +
            "ORDER BY r.fechaEmision DESC")
    List<RecetaDetalleDTO> listarRecetasConDetalle();
}
