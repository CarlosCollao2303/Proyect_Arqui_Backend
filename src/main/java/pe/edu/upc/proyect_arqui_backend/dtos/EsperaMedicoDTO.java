package pe.edu.upc.proyect_arqui_backend.dtos;

// Reporte: cuanto esperan en promedio los pacientes de cada medico (solo citas atendidas).
// Junta la carga de trabajo (citas atendidas) con la calidad de atencion (minutos de espera).
public class EsperaMedicoDTO {
    private String medico;
    private String especialidad;
    private Long citasAtendidas;
    private Double esperaPromedio;
    private Integer esperaMaxima;

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

    public Long getCitasAtendidas() {
        return citasAtendidas;
    }

    public void setCitasAtendidas(Long citasAtendidas) {
        this.citasAtendidas = citasAtendidas;
    }

    public Double getEsperaPromedio() {
        return esperaPromedio;
    }

    public void setEsperaPromedio(Double esperaPromedio) {
        this.esperaPromedio = esperaPromedio;
    }

    public Integer getEsperaMaxima() {
        return esperaMaxima;
    }

    public void setEsperaMaxima(Integer esperaMaxima) {
        this.esperaMaxima = esperaMaxima;
    }
}
