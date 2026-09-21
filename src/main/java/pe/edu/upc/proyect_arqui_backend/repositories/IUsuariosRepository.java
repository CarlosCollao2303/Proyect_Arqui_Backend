package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.util.Optional;

public interface IUsuariosRepository extends JpaRepository<Usuarios, Integer> {
    Optional<Usuarios> findByCorreo(String correo);
}
