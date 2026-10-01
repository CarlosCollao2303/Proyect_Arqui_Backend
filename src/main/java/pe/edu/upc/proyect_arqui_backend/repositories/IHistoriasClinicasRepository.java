package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;

import java.util.Optional;

public interface IHistoriasClinicasRepository extends JpaRepository<HistoriasClinicas, Integer> {
    @Query(value = "SELECT * FROM historiales_clinicos WHERE paciente_id = :idPaciente", nativeQuery = true)
    Optional<HistoriasClinicas> findByPacienteIdNative(@Param("idPaciente") int idPaciente);
}
