package pe.edu.upc.proyect_arqui_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class DiagnosticoDTO {
    private int idDiagnostico;

    @NotNull(message = "El id del detalle de historial es obligatorio")
    private Integer idDetalleHistorial;

    @NotBlank(message = "El codigo CIE-10 es obligatorio")
    private String codigoCie10;

    private String descripcion;
    private LocalDateTime fechaRegistro;

    public int getIdDiagnostico() {
        return idDiagnostico;
    }

    public void setIdDiagnostico(int idDiagnostico) {
        this.idDiagnostico = idDiagnostico;
    }

    public Integer getIdDetalleHistorial() {
        return idDetalleHistorial;
    }

    public void setIdDetalleHistorial(Integer idDetalleHistorial) {
        this.idDetalleHistorial = idDetalleHistorial;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
