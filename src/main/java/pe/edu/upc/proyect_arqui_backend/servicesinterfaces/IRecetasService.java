package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Recetas;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IRecetasService {
    public void insert(Recetas r);
    public void delete(int id);
    public void update(Recetas r);
    public List<Recetas> list();
    public Optional<Recetas> listId(int id);
    public List<Recetas> listByMedicamento(String medicamento);
    public List<Recetas> listByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);
}
