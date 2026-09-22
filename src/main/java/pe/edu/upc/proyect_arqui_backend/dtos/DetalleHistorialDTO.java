package pe.edu.upc.proyect_arqui_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class DetalleHistorialDTO {
    private int idDetalleHistorial;

    @NotNull(message = "El id de la historia clinica es obligatorio")
    private Integer idHistoriaClinica;

    @NotNull(message = "El id del medico es obligatorio")
    private Integer idMedico;

    @NotBlank(message = "El motivo de consulta es obligatorio")
    private String motivoConsulta;

    private String cifradoDatos;
    private LocalDateTime fechaRegistro;

    public int getIdDetalleHistorial() {
        return idDetalleHistorial;
    }

    public void setIdDetalleHistorial(int idDetalleHistorial) {
        this.idDetalleHistorial = idDetalleHistorial;
    }

    public Integer getIdHistoriaClinica() {
        return idHistoriaClinica;
    }

    public void setIdHistoriaClinica(Integer idHistoriaClinica) {
        this.idHistoriaClinica = idHistoriaClinica;
    }

    public Integer getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Integer idMedico) {
        this.idMedico = idMedico;
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
