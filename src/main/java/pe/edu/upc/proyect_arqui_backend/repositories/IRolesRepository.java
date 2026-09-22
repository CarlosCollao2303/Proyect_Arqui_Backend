package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;

import java.util.Optional;

public interface IRolesRepository extends JpaRepository<Roles, Integer> {
    Optional<Roles> findByNombre(String nombre);
}
