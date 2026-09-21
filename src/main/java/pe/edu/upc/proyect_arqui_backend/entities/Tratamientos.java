package pe.edu.upc.proyect_arqui_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "tratamientos")
public class Tratamientos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idTratamiento;

    @ManyToOne
    @JoinColumn(name = "detalle_id", nullable = false)
    private DetalleHistorial detalleHistorial;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "estado")
    private String estado;

    public Tratamientos() {
    }

    public Tratamientos(int idTratamiento, DetalleHistorial detalleHistorial, String descripcion, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.idTratamiento = idTratamiento;
        this.detalleHistorial = detalleHistorial;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getIdTratamiento() {
        return idTratamiento;
    }

    public void setIdTratamiento(int idTratamiento) {
        this.idTratamiento = idTratamiento;
    }

    public DetalleHistorial getDetalleHistorial() {
        return detalleHistorial;
    }

    public void setDetalleHistorial(DetalleHistorial detalleHistorial) {
        this.detalleHistorial = detalleHistorial;
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
