package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;

import java.util.List;
import java.util.Optional;

public interface IHistoriasClinicasService {
    public void insert(HistoriasClinicas h);
    public void delete(int id);
    public void update(HistoriasClinicas h);
    public List<HistoriasClinicas> list();
    public Optional<HistoriasClinicas> listId(int id);
}
