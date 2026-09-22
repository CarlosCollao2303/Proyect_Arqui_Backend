package pe.edu.upc.proyect_arqui_backend.dtos;

import jakarta.validation.constraints.NotBlank;

public class RolesDTO {
    private int idRol;

    @NotBlank(message = "El nombre del rol es obligatorio")
    private String nombre;

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
