package pe.edu.upc.proyect_arqui_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "detalles_historial")
public class DetalleHistorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idDetalleHistorial;

    @ManyToOne
    @JoinColumn(name = "historial_id", nullable = false)
    private HistoriasClinicas historiaClinica;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Usuarios medico;

    @ManyToOne
    @JoinColumn(name = "examen_id")
    private Examenes examen;

    @ManyToOne
    @JoinColumn(name = "diagnostico_id")
    private Diagnostico diagnostico;

    @ManyToOne
    @JoinColumn(name = "tratamiento_id")
    private Tratamientos tratamiento;

    @ManyToOne
    @JoinColumn(name = "receta_id")
    private Recetas receta;

    @Column(name = "motivo_consulta")
    private String motivoConsulta;

    @Column(name = "cifrado_datos", columnDefinition = "TEXT")
    private String cifradoDatos;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    public DetalleHistorial() {
    }

    public DetalleHistorial(int idDetalleHistorial, HistoriasClinicas historiaClinica, Usuarios medico, String motivoConsulta, String cifradoDatos, LocalDateTime fechaRegistro) {
        this.idDetalleHistorial = idDetalleHistorial;
        this.historiaClinica = historiaClinica;
        this.medico = medico;
        this.motivoConsulta = motivoConsulta;
        this.cifradoDatos = cifradoDatos;
        this.fechaRegistro = fechaRegistro;
    }

    public int getIdDetalleHistorial() {
        return idDetalleHistorial;
    }

    public void setIdDetalleHistorial(int idDetalleHistorial) {
        this.idDetalleHistorial = idDetalleHistorial;
    }

    public HistoriasClinicas getHistoriaClinica() {
        return historiaClinica;
    }

    public void setHistoriaClinica(HistoriasClinicas historiaClinica) {
        this.historiaClinica = historiaClinica;
    }

    public Usuarios getMedico() {
        return medico;
    }

    public void setMedico(Usuarios medico) {
        this.medico = medico;
    }

    public Examenes getExamen() {
        return examen;
    }

    public void setExamen(Examenes examen) {
        this.examen = examen;
    }

    public Diagnostico getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(Diagnostico diagnostico) {
        this.diagnostico = diagnostico;
    }

    public Tratamientos getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(Tratamientos tratamiento) {
        this.tratamiento = tratamiento;
    }

    public Recetas getReceta() {
        return receta;
    }

    public void setReceta(Recetas receta) {
        this.receta = receta;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getCifradoDatos() {
        return cifradoDatos;
    }

    public void setCifradoDatos(String cifradoDatos) {
        this.cifradoDatos = cifradoDatos;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
