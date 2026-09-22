package pe.edu.upc.proyect_arqui_backend.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class HistoriasClinicasDTO {
    private int idHistoriaClinica;

    @NotNull(message = "El id del paciente es obligatorio")
    private Integer idPaciente;

    private LocalDateTime fechaCreacion;

    public int getIdHistoriaClinica() {
        return idHistoriaClinica;
    }

    public void setIdHistoriaClinica(int idHistoriaClinica) {
        this.idHistoriaClinica = idHistoriaClinica;
    }

    public Integer getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Integer idPaciente) {
        this.idPaciente = idPaciente;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
