package pe.edu.upc.proyect_arqui_backend.dtos;

// Reporte: cuanto tarda cada tipo de examen en tener resultado y cuantos siguen pendientes.
// Junta el volumen (examenes solicitados) con la rapidez de entrega (dias hasta el resultado).
public class TiempoExamenDTO {
    private String tipoExamen;
    private Long solicitados;
    private Long conResultado;
    private Long pendientes;
    // Dias con un decimal. Son null si ningun examen de ese tipo tiene resultado todavia.
    private Double diasPromedio;
    private Double diasMaximo;

    public String getTipoExamen() {
        return tipoExamen;
    }

    public void setTipoExamen(String tipoExamen) {
        this.tipoExamen = tipoExamen;
    }

    public Long getSolicitados() {
        return solicitados;
    }

    public void setSolicitados(Long solicitados) {
        this.solicitados = solicitados;
    }

    public Long getConResultado() {
        return conResultado;
    }

    public void setConResultado(Long conResultado) {
        this.conResultado = conResultado;
    }

    public Long getPendientes() {
        return pendientes;
    }

    public void setPendientes(Long pendientes) {
        this.pendientes = pendientes;
    }

    public Double getDiasPromedio() {
        return diasPromedio;
    }

    public void setDiasPromedio(Double diasPromedio) {
        this.diasPromedio = diasPromedio;
    }

    public Double getDiasMaximo() {
        return diasMaximo;
    }

    public void setDiasMaximo(Double diasMaximo) {
        this.diasMaximo = diasMaximo;
    }
}
