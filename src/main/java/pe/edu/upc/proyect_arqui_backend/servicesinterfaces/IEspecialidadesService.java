package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;

import java.util.List;
import java.util.Optional;

public interface IEspecialidadesService {
    public void insert(Especialidades e);
    public void delete(int id);
    public void update(Especialidades e);
    public List<Especialidades> list();
    public Optional<Especialidades> listId(int id);
}
