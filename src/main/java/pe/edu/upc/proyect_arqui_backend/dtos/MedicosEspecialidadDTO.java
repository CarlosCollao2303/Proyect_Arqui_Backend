package pe.edu.upc.proyect_arqui_backend.dtos;

// Reporte: cuantos medicos tiene cada especialidad, separados en activos e inactivos.
// Sirve para ver que especialidades tienen mas cobertura y cuales se quedaron sin medicos.
// idEspecialidad es null en la fila "Sin especialidad" (medicos que aun no tienen una).
public class MedicosEspecialidadDTO {
    private Integer idEspecialidad;
    private String especialidad;
    private Long activos;
    private Long inactivos;
    private Long total;

    public Integer getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(Integer idEspecialidad) {
        this.idEspecialidad = idEspecialidad;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Long getActivos() {
        return activos;
    }

    public void setActivos(Long activos) {
        this.activos = activos;
    }

    public Long getInactivos() {
        return inactivos;
    }

    public void setInactivos(Long inactivos) {
        this.inactivos = inactivos;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
