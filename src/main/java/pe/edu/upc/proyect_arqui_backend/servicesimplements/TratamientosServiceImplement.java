package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;
import pe.edu.upc.proyect_arqui_backend.repositories.ITratamientosRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ITratamientosService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TratamientosServiceImplement implements ITratamientosService {

    private final ITratamientosRepository tR;

    public TratamientosServiceImplement(ITratamientosRepository tR) {
        this.tR = tR;
    }

    @Override
    public void insert(Tratamientos t) {
        tR.save(t);
    }

    @Override
    public void delete(int id) {
        tR.deleteById(id);
    }

    @Override
    public void update(Tratamientos t) {
        tR.save(t);
    }

    @Override
    public List<Tratamientos> list() {
        return tR.findAll();
    }

    @Override
    public Optional<Tratamientos> listId(int id) {
        return tR.findById(id);
    }
}
