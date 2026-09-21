package pe.edu.upc.proyect_arqui_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Citas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idCita;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuarios paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Usuarios medico;

    @Column(name = "fecha_hora_programada")
    private LocalDateTime fechaHoraProgramada;

    @Column(name = "tiempo_espera_minutos")
    private int tiempoEsperaMinutos;

    @Column(name = "estado")
    private String estado;

    public Citas() {
    }

    public Citas(int idCita, Usuarios paciente, Usuarios medico, LocalDateTime fechaHoraProgramada, int tiempoEsperaMinutos, String estado) {
        this.idCita = idCita;
        this.paciente = paciente;
        this.medico = medico;
        this.fechaHoraProgramada = fechaHoraProgramada;
        this.tiempoEsperaMinutos = tiempoEsperaMinutos;
        this.estado = estado;
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public Usuarios getPaciente() {
        return paciente;
    }

    public void setPaciente(Usuarios paciente) {
        this.paciente = paciente;
    }

    public Usuarios getMedico() {
        return medico;
    }

    public void setMedico(Usuarios medico) {
        this.medico = medico;
    }

    public LocalDateTime getFechaHoraProgramada() {
        return fechaHoraProgramada;
    }

    public void setFechaHoraProgramada(LocalDateTime fechaHoraProgramada) {
        this.fechaHoraProgramada = fechaHoraProgramada;
    }

    public int getTiempoEsperaMinutos() {
        return tiempoEsperaMinutos;
    }

    public void setTiempoEsperaMinutos(int tiempoEsperaMinutos) {
        this.tiempoEsperaMinutos = tiempoEsperaMinutos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
