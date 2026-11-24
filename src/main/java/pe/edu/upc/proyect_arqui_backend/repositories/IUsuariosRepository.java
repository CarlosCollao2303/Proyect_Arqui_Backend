package pe.edu.upc.proyect_arqui_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.util.List;
import java.util.Optional;

public interface IUsuariosRepository extends JpaRepository<Usuarios, Integer> {
    Optional<Usuarios> findByCorreo(String correo);
    boolean existsByRolIdRol(int idRol);
    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdUsuarioNot(String correo, int idUsuario);

    // Usuarios de un rol (PACIENTE, MEDICO...) ordenados por apellido.
    List<Usuarios> findByRol_NombreOrderByApellidosAscNombresAsc(String nombreRol);

    // Pacientes asignados a un medico: los que tienen al menos una cita con el.
    @Query("SELECT DISTINCT p FROM Citas c " +
            "JOIN c.paciente p " +
            "WHERE c.medico.correo = :correoMedico " +
            "ORDER BY p.apellidos ASC, p.nombres ASC")
    List<Usuarios> findPacientesDelMedico(@Param("correoMedico") String correoMedico);

    // Para comparar sin tildes: TRANSLATE cambia cada vocal con tilde (y la enie) por la
    // letra sin tilde. Existe en PostgreSQL y en H2 (la base de los tests).
    String CON_TILDES = "\u00e1\u00e9\u00ed\u00f3\u00fa\u00fc\u00f1";
    String SIN_TILDES = "aeiouun";

    // Busca por nombre completo (sin importar mayusculas ni tildes) o por DNI. Si rol es
    // null busca en todos los roles. El CAST es necesario en PostgreSQL para el
    // ":rol IS NULL" (ver ICitasRepository).
    @Query("SELECT u FROM Usuarios u " +
            "WHERE (CAST(:rol AS String) IS NULL OR u.rol.nombre = :rol) " +
            "AND (CAST(FUNCTION('TRANSLATE', LOWER(CONCAT(u.nombres, ' ', u.apellidos)), '" + CON_TILDES + "', '" + SIN_TILDES + "') AS String) " +
            "LIKE CONCAT('%', CAST(FUNCTION('TRANSLATE', LOWER(:texto), '" + CON_TILDES + "', '" + SIN_TILDES + "') AS String), '%') " +
            "OR u.dni LIKE CONCAT('%', :texto, '%')) " +
            "ORDER BY u.apellidos ASC, u.nombres ASC")
    List<Usuarios> buscarPorNombreODni(@Param("texto") String texto, @Param("rol") String rol);
}
