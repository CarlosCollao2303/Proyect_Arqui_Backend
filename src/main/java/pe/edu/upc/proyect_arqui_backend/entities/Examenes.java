package pe.edu.upc.proyect_arqui_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "examenes")
public class Examenes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idExamen;

    @ManyToOne
    @JoinColumn(name = "detalle_id", nullable = false)
    private DetalleHistorial detalleHistorial;

    @Column(name = "tipo_examen")
    private String tipoExamen;

    @Column(name = "resultado", columnDefinition = "TEXT")
    private String resultado;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_resultado")
    private LocalDateTime fechaResultado;

    public Examenes() {
    }

    public Examenes(int idExamen, DetalleHistorial detalleHistorial, String tipoExamen, String resultado, LocalDateTime fechaSolicitud, LocalDateTime fechaResultado) {
        this.idExamen = idExamen;
        this.detalleHistorial = detalleHistorial;
        this.tipoExamen = tipoExamen;
        this.resultado = resultado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaResultado = fechaResultado;
    }

    public int getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(int idExamen) {
        this.idExamen = idExamen;
    }

    public DetalleHistorial getDetalleHistorial() {
        return detalleHistorial;
    }

    public void setDetalleHistorial(DetalleHistorial detalleHistorial) {
        this.detalleHistorial = detalleHistorial;
    }

    public String getTipoExamen() {
        return tipoExamen;
    }

    public void setTipoExamen(String tipoExamen) {
        this.tipoExamen = tipoExamen;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaResultado() {
        return fechaResultado;
    }

    public void setFechaResultado(LocalDateTime fechaResultado) {
        this.fechaResultado = fechaResultado;
    }
}
