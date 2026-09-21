package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Roles;

import java.util.List;
import java.util.Optional;

public interface IRolesService {
    public List<Roles> list();
    public Optional<Roles> listId(int id);
}
