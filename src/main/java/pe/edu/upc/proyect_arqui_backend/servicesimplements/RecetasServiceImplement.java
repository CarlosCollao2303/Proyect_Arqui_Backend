package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Recetas;
import pe.edu.upc.proyect_arqui_backend.repositories.IRecetasRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRecetasService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecetasServiceImplement implements IRecetasService {

    private final IRecetasRepository rR;

    public RecetasServiceImplement(IRecetasRepository rR) {
        this.rR = rR;
    }

    @Override
    public void insert(Recetas r) {
        rR.save(r);
    }

    @Override
    public void delete(int id) {
        rR.deleteById(id);
    }

    @Override
    public void update(Recetas r) {
        rR.save(r);
    }

    @Override
    public List<Recetas> list() {
        return rR.findAll();
    }

    @Override
    public Optional<Recetas> listId(int id) {
        return rR.findById(id);
    }
}
