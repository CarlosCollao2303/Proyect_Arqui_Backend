package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Examenes;
import pe.edu.upc.proyect_arqui_backend.repositories.IExamenesRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IExamenesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExamenesServiceImplement implements IExamenesService {

    private final IExamenesRepository eR;

    public ExamenesServiceImplement(IExamenesRepository eR) {
        this.eR = eR;
    }

    @Override
    public void insert(Examenes e) {
        eR.save(e);
    }

    @Override
    public void delete(int id) {
        eR.deleteById(id);
    }

    @Override
    public void update(Examenes e) {
        eR.save(e);
    }

    @Override
    public List<Examenes> list() {
        return eR.findAll();
    }

    @Override
    public Optional<Examenes> listId(int id) {
        return eR.findById(id);
    }
}
