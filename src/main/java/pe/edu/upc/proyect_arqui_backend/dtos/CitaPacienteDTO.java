package pe.edu.upc.proyect_arqui_backend.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CitaPacienteDTO {
    private int idCita;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    private String medico;
    private String especialidad;

    public CitaPacienteDTO() {
    }

    public CitaPacienteDTO(int idCita, LocalDateTime fechaHoraProgramada, String estado,
                           String medico, String especialidad) {
        this.idCita = idCita;
        this.fecha = fechaHoraProgramada != null ? fechaHoraProgramada.toLocalDate() : null;
        this.hora = fechaHoraProgramada != null ? fechaHoraProgramada.toLocalTime() : null;
        this.estado = estado;
        this.medico = medico;
        this.especialidad = especialidad;
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
}
