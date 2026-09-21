package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;

import java.util.Optional;

public interface IEspecialidadesService {
    public Optional<Especialidades> listId(int id);
}
