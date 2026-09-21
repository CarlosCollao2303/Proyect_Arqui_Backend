package pe.edu.upc.proyect_arqui_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historiales_clinicos")
public class HistoriasClinicas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idHistoriaClinica;

    @OneToOne
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Usuarios paciente;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    public HistoriasClinicas() {
    }

    public HistoriasClinicas(int idHistoriaClinica, Usuarios paciente, LocalDateTime fechaCreacion) {
        this.idHistoriaClinica = idHistoriaClinica;
        this.paciente = paciente;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdHistoriaClinica() {
        return idHistoriaClinica;
    }

    public void setIdHistoriaClinica(int idHistoriaClinica) {
        this.idHistoriaClinica = idHistoriaClinica;
    }

    public Usuarios getPaciente() {
        return paciente;
    }

    public void setPaciente(Usuarios paciente) {
        this.paciente = paciente;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
