package pe.edu.upc.proyect_arqui_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recetas")
public class Recetas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idReceta;

    @ManyToOne
    @JoinColumn(name = "detalle_id", nullable = false)
    private DetalleHistorial detalleHistorial;

    @Column(name = "medicamento")
    private String medicamento;

    @Column(name = "dosis")
    private String dosis;

    @Column(name = "frecuencia")
    private String frecuencia;

    @Column(name = "duracion_dias")
    private int duracionDias;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    public Recetas() {
    }

    public Recetas(int idReceta, DetalleHistorial detalleHistorial, String medicamento, String dosis, String frecuencia, int duracionDias, String indicaciones, LocalDateTime fechaEmision) {
        this.idReceta = idReceta;
        this.detalleHistorial = detalleHistorial;
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.duracionDias = duracionDias;
        this.indicaciones = indicaciones;
        this.fechaEmision = fechaEmision;
    }

    public int getIdReceta() {
        return idReceta;
    }

    public void setIdReceta(int idReceta) {
        this.idReceta = idReceta;
    }

    public DetalleHistorial getDetalleHistorial() {
        return detalleHistorial;
    }

    public void setDetalleHistorial(DetalleHistorial detalleHistorial) {
        this.detalleHistorial = detalleHistorial;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(int duracionDias) {
        this.duracionDias = duracionDias;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}
