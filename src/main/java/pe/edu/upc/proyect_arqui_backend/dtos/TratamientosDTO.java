package pe.edu.upc.proyect_arqui_backend.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TratamientosDTO {
    private int idTratamiento;

    @NotNull(message = "El id del detalle de historial es obligatorio")
    private Integer idDetalleHistorial;

    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    public int getIdTratamiento() {
        return idTratamiento;
    }

    public void setIdTratamiento(int idTratamiento) {
        this.idTratamiento = idTratamiento;
    }

    public Integer getIdDetalleHistorial() {
        return idDetalleHistorial;
    }

    public void setIdDetalleHistorial(Integer idDetalleHistorial) {
        this.idDetalleHistorial = idDetalleHistorial;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
