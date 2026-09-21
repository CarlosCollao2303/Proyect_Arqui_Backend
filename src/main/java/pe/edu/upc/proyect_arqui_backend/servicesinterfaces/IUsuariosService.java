package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;

import java.util.List;

public interface IUsuariosService {
    public void insert(Usuarios u);
    public void delete(int id);
    public void update(Usuarios u);
    public List<Usuarios> list();
}
