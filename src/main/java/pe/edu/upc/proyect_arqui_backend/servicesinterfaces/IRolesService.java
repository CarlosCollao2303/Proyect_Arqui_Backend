package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Roles;

import java.util.List;
import java.util.Optional;

public interface IRolesService {
    public void insert(Roles r);
    public void delete(int id);
    public void update(Roles r);
    public List<Roles> list();
    public Optional<Roles> listId(int id);
    public Optional<Roles> listByNombre(String nombre);
}
