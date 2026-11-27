package pe.edu.upc.proyect_arqui_backend.dtos;

public class DiagnosticoFrecuenteDTO {
    private String codigoCie10;
    private String descripcion;
    private Long cantidad;

    public DiagnosticoFrecuenteDTO() {
    }

    public DiagnosticoFrecuenteDTO(String codigoCie10, String descripcion, Long cantidad) {
        this.codigoCie10 = codigoCie10;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
    }

    public String getCodigoCie10() {
        return codigoCie10;
    }

    public void setCodigoCie10(String codigoCie10) {
        this.codigoCie10 = codigoCie10;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }
}
