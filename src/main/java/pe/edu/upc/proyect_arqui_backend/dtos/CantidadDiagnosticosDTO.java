package pe.edu.upc.proyect_arqui_backend.dtos;

public class CantidadDiagnosticosDTO {
    private int idHistoriaClinica;
    private String paciente;
    private long cantidadDiagnosticos;

    public CantidadDiagnosticosDTO() {
    }

    public CantidadDiagnosticosDTO(int idHistoriaClinica, String paciente, Long cantidadDiagnosticos) {
        this.idHistoriaClinica = idHistoriaClinica;
        this.paciente = paciente;
        this.cantidadDiagnosticos = cantidadDiagnosticos != null ? cantidadDiagnosticos : 0;
    }

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

    public long getCantidadDiagnosticos() {
        return cantidadDiagnosticos;
    }

    public void setCantidadDiagnosticos(long cantidadDiagnosticos) {
        this.cantidadDiagnosticos = cantidadDiagnosticos;
    }
}
