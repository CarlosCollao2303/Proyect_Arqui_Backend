package pe.edu.upc.proyect_arqui_backend.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CitasDTO {
    private int idCita;

    // Sin @NotNull: si quien registra es PACIENTE se ignora y se usa su propio id.
    // CitasController lo exige a mano para ADMIN y MEDICO.
    private Integer idPaciente;

    @NotNull(message = "El id del medico es obligatorio")
    private Integer idMedico;

    @NotNull(message = "La fecha y hora programada es obligatoria")
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
