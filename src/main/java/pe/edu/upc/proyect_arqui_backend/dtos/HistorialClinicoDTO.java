package pe.edu.upc.proyect_arqui_backend.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;

// Historial clinico completo de un paciente, armado a partir de su historia y sus detalles.
// En la vista del paciente solo se llenan citas, examenes y recetas; las demas listas quedan
// en null y NON_NULL hace que ni siquiera aparezcan en el JSON.
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistorialClinicoDTO {
    private int idHistoriaClinica;
    private String paciente;
    private String dni;
    private LocalDate fechaCreacion;

    private List<CitaEstadoDTO> citas;
    private List<ExamenesDTO> examenes;
    private List<RecetasDTO> recetas;

    // Solo para el personal (ADMIN y MEDICO)
    private List<ConsultaHistorialDTO> consultas;
    private List<DiagnosticoDTO> diagnosticos;
    private List<TratamientosDTO> tratamientos;

    public int getIdHistoriaClinica() {
        return idHistoriaClinica;
    }

    public void setIdHistoriaClinica(int idHistoriaClinica) {
        this.idHistoriaClinica = idHistoriaClinica;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<CitaEstadoDTO> getCitas() {
        return citas;
    }

    public void setCitas(List<CitaEstadoDTO> citas) {
        this.citas = citas;
    }

    public List<ExamenesDTO> getExamenes() {
        return examenes;
    }

    public void setExamenes(List<ExamenesDTO> examenes) {
        this.examenes = examenes;
    }

    public List<RecetasDTO> getRecetas() {
        return recetas;
    }

    public void setRecetas(List<RecetasDTO> recetas) {
        this.recetas = recetas;
    }

    public List<ConsultaHistorialDTO> getConsultas() {
        return consultas;
    }

    public void setConsultas(List<ConsultaHistorialDTO> consultas) {
        this.consultas = consultas;
    }

    public List<DiagnosticoDTO> getDiagnosticos() {
        return diagnosticos;
    }

    public void setDiagnosticos(List<DiagnosticoDTO> diagnosticos) {
        this.diagnosticos = diagnosticos;
    }

    public List<TratamientosDTO> getTratamientos() {
        return tratamientos;
    }

    public void setTratamientos(List<TratamientosDTO> tratamientos) {
        this.tratamientos = tratamientos;
    }
}
