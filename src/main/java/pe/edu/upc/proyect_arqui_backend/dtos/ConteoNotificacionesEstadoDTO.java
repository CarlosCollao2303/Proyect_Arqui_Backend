package pe.edu.upc.proyect_arqui_backend.dtos;

public class ConteoNotificacionesEstadoDTO {
    private String estadoEnvio;
    private Long total;

    public ConteoNotificacionesEstadoDTO() {
    }

    public ConteoNotificacionesEstadoDTO(String estadoEnvio, Long total) {
        this.estadoEnvio = estadoEnvio;
        this.total = total;
    }

    public String getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(String estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
