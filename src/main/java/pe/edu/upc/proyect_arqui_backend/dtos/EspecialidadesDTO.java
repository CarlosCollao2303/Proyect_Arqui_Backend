package pe.edu.upc.proyect_arqui_backend.dtos;

import jakarta.validation.constraints.NotBlank;

public class EspecialidadesDTO {
    private int idEspecialidad;

    @NotBlank(message = "El nombre de la especialidad es obligatorio")
    private String nombre;

    public int getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(int idEspecialidad) {
        this.idEspecialidad = idEspecialidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
