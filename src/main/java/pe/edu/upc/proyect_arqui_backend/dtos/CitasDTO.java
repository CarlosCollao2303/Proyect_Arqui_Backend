package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDateTime;

public class CitasDTO {
    private int idCita;
    private Integer idPaciente;
    private Integer idMedico;
    private LocalDateTime fechaHoraProgramada;
    private int tiempoEsperaMinutos;
    private String estado;

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public Integer getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Integer idPaciente) {
        this.idPaciente = idPaciente;
    }

    public Integer getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Integer idMedico) {
        this.idMedico = idMedico;
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
