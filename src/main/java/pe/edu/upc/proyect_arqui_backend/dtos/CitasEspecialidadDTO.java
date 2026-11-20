package pe.edu.upc.proyect_arqui_backend.dtos;

// Reporte: cuantas citas tiene cada especialidad, separadas por estado.
// Sirve para ver que especialidades tienen mas demanda y en cuales se cancelan mas citas.
public class CitasEspecialidadDTO {
    private String especialidad;
    private Long atendidas;
    private Long pendientes;
    private Long canceladas;
    private Long total;

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Long getAtendidas() {
        return atendidas;
    }

    public void setAtendidas(Long atendidas) {
        this.atendidas = atendidas;
    }

    public Long getPendientes() {
        return pendientes;
    }

    public void setPendientes(Long pendientes) {
        this.pendientes = pendientes;
    }

    public Long getCanceladas() {
        return canceladas;
    }

    public void setCanceladas(Long canceladas) {
        this.canceladas = canceladas;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
