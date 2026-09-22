package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;

import java.util.List;
import java.util.Optional;

public interface IDetalleHistorialService {
    public void insert(DetalleHistorial d);
    public void delete(int id);
    public void update(DetalleHistorial d);
    public List<DetalleHistorial> list();
    public Optional<DetalleHistorial> listId(int id);
}
