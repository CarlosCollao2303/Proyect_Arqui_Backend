package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Examenes;

import java.util.List;
import java.util.Optional;

public interface IExamenesService {
    public void insert(Examenes e);
    public void delete(int id);
    public void update(Examenes e);
    public List<Examenes> list();
    public Optional<Examenes> listId(int id);
}
