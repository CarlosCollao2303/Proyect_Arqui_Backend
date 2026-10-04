package pe.edu.upc.proyect_arqui_backend.dtos;

public class CantidadExamenesDTO {
    private int idPaciente;
    private String paciente;
    private String dni;
    private long cantidadExamenes;

    public CantidadExamenesDTO() {
    }

    public CantidadExamenesDTO(int idPaciente, String paciente, String dni, Long cantidadExamenes) {
        this.idPaciente = idPaciente;
        this.paciente = paciente;
        this.dni = dni;
        this.cantidadExamenes = cantidadExamenes != null ? cantidadExamenes : 0;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
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

    public long getCantidadExamenes() {
        return cantidadExamenes;
    }

    public void setCantidadExamenes(long cantidadExamenes) {
        this.cantidadExamenes = cantidadExamenes;
    }
}
