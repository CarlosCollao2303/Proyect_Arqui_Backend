package pe.edu.upc.proyect_arqui_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class DetalleHistorialDTO {
    private int idDetalleHistorial;

    @NotNull(message = "El id de la historia clinica es obligatorio")
    private Integer idHistoriaClinica;

    @NotNull(message = "El id del medico es obligatorio")
    private Integer idMedico;

    @Positive(message = "El id del examen debe ser positivo")
    private Integer idExamen;

    @Positive(message = "El id del diagnostico debe ser positivo")
    private Integer idDiagnostico;

    @Positive(message = "El id del tratamiento debe ser positivo")
    private Integer idTratamiento;

    @Positive(message = "El id de la receta debe ser positivo")
    private Integer idReceta;

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

    public Integer getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(Integer idExamen) {
        this.idExamen = idExamen;
    }

    public Integer getIdDiagnostico() {
        return idDiagnostico;
    }

    public void setIdDiagnostico(Integer idDiagnostico) {
        this.idDiagnostico = idDiagnostico;
    }

    public Integer getIdTratamiento() {
        return idTratamiento;
    }

    public void setIdTratamiento(Integer idTratamiento) {
        this.idTratamiento = idTratamiento;
    }

    public Integer getIdReceta() {
        return idReceta;
    }

    public void setIdReceta(Integer idReceta) {
        this.idReceta = idReceta;
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
