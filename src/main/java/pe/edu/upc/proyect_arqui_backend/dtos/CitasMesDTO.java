package pe.edu.upc.proyect_arqui_backend.dtos;

// Reporte: citas de cada mes de un año, separadas por estado.
// Sirve para ver como cambia la demanda en el tiempo y si las cancelaciones suben o bajan.
public class CitasMesDTO {
    // 1 = enero ... 12 = diciembre
    private Integer mes;
    private Long atendidas;
    private Long pendientes;
    private Long canceladas;
    private Long total;

    public CitasMesDTO() {
    }

    // Mes sin citas: todo en cero
    public CitasMesDTO(Integer mes) {
        this.mes = mes;
        this.atendidas = 0L;
        this.pendientes = 0L;
        this.canceladas = 0L;
        this.total = 0L;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
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
