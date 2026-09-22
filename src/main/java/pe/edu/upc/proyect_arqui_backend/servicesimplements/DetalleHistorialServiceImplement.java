package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.repositories.IDetalleHistorialRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetalleHistorialServiceImplement implements IDetalleHistorialService {

    private final IDetalleHistorialRepository dhR;

    public DetalleHistorialServiceImplement(IDetalleHistorialRepository dhR) {
        this.dhR = dhR;
    }

    @Override
    public void insert(DetalleHistorial d) {
        dhR.save(d);
    }

    @Override
    public void delete(int id) {
        dhR.deleteById(id);
    }

    @Override
    public void update(DetalleHistorial d) {
        dhR.save(d);
    }

    @Override
    public List<DetalleHistorial> list() {
        return dhR.findAll();
    }

    @Override
    public Optional<DetalleHistorial> listId(int id) {
        return dhR.findById(id);
    }
}
