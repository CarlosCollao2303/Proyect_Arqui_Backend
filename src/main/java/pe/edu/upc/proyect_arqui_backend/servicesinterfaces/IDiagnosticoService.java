package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Diagnostico;

import java.util.List;
import java.util.Optional;

public interface IDiagnosticoService {
    public void insert(Diagnostico d);
    public void delete(int id);
    public void update(Diagnostico d);
    public List<Diagnostico> list();
    public Optional<Diagnostico> listId(int id);
}
