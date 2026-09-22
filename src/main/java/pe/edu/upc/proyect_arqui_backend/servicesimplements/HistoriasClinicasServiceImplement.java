package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;
import pe.edu.upc.proyect_arqui_backend.repositories.IHistoriasClinicasRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IHistoriasClinicasService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoriasClinicasServiceImplement implements IHistoriasClinicasService {

    private final IHistoriasClinicasRepository hR;

    public HistoriasClinicasServiceImplement(IHistoriasClinicasRepository hR) {
        this.hR = hR;
    }

    @Override
    public void insert(HistoriasClinicas h) {
        hR.save(h);
    }

    @Override
    public void delete(int id) {
        hR.deleteById(id);
    }

    @Override
    public void update(HistoriasClinicas h) {
        hR.save(h);
    }

    @Override
    public List<HistoriasClinicas> list() {
        return hR.findAll();
    }

    @Override
    public Optional<HistoriasClinicas> listId(int id) {
        return hR.findById(id);
    }
}
