package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;

import java.util.List;
import java.util.Optional;

public interface ITratamientosService {
    public void insert(Tratamientos t);
    public void delete(int id);
    public void update(Tratamientos t);
    public List<Tratamientos> list();
    public Optional<Tratamientos> listId(int id);
}
