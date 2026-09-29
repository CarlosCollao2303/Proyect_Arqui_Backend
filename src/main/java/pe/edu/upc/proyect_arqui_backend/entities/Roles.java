package pe.edu.upc.proyect_arqui_backend.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "roles")
public class Roles {
    // Roles que usan los @PreAuthorize y el registro publico: se crean al arrancar
    // (DataInitializer) y no se pueden renombrar ni eliminar desde la app.
    public static final List<String> ROLES_BASE = List.of("ADMIN", "MEDICO", "PACIENTE");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idRol;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    public Roles() {
    }

    public Roles(int idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
