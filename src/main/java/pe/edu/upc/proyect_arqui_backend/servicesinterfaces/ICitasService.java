package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.Citas;

import java.util.List;
import java.util.Optional;

public interface ICitasService {
    public void insert(Citas c);
    public void delete(int id);
    public void update(Citas c);
    public List<Citas> list();
    public Optional<Citas> listId(int id);
    public List<Citas> listByParticipanteCorreo(String correo);
}
