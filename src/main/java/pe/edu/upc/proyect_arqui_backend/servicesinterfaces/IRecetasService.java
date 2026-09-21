package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Recetas;

import java.util.List;

public interface IRecetasService {
    public void insert(Recetas r);
    public void delete(int id);
    public void update(Recetas r);
    public List<Recetas> list();
}
