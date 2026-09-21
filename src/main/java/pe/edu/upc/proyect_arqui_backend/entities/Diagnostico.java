package pe.edu.upc.proyect_arqui_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosticos")
public class Diagnostico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idDiagnostico;

    @ManyToOne
    @JoinColumn(name = "detalle_id", nullable = false)
    private DetalleHistorial detalleHistorial;

    @Column(name = "codigo_cie10")
    private String codigoCie10;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    public Diagnostico() {
    }

    public Diagnostico(int idDiagnostico, DetalleHistorial detalleHistorial, String codigoCie10, String descripcion, LocalDateTime fechaRegistro) {
        this.idDiagnostico = idDiagnostico;
        this.detalleHistorial = detalleHistorial;
        this.codigoCie10 = codigoCie10;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
    }

    public int getIdDiagnostico() {
        return idDiagnostico;
    }

    public void setIdDiagnostico(int idDiagnostico) {
        this.idDiagnostico = idDiagnostico;
    }

    public DetalleHistorial getDetalleHistorial() {
        return detalleHistorial;
    }

    public void setDetalleHistorial(DetalleHistorial detalleHistorial) {
        this.detalleHistorial = detalleHistorial;
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
