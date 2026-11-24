package pe.edu.upc.proyect_arqui_backend.dtos;

import jakarta.validation.constraints.NotNull;

// Lo unico que hace falta para notificar una cita: cual es. El tipo y el mensaje
// los arma el back con los datos de la cita.
public class NotificacionCitaDTO {
    @NotNull(message = "El id de la cita es obligatorio")
    private Integer idCita;

    public Integer getIdCita() {
        return idCita;
    }

    public void setIdCita(Integer idCita) {
        this.idCita = idCita;
    }
}
