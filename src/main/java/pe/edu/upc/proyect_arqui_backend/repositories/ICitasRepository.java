package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;

import java.util.List;

public interface ICitasRepository extends JpaRepository<Citas, Integer> {
    // Citas donde el usuario es el paciente o el medico, de la mas proxima a la mas lejana.
    @Query("SELECT c FROM Citas c " +
            "WHERE c.paciente.correo = :correo OR c.medico.correo = :correo " +
            "ORDER BY c.fechaHoraProgramada ASC")
    List<Citas> findByParticipanteCorreo(@Param("correo") String correo);
}
