package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Diagnostico;
import pe.edu.upc.proyect_arqui_backend.repositories.IDiagnosticoRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDiagnosticoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiagnosticoServiceImplement implements IDiagnosticoService {

    private final IDiagnosticoRepository dR;

    public DiagnosticoServiceImplement(IDiagnosticoRepository dR) {
        this.dR = dR;
    }

    @Override
    public void insert(Diagnostico d) {
        dR.save(d);
    }

    @Override
    public void delete(int id) {
        dR.deleteById(id);
    }

    @Override
    public void update(Diagnostico d) {
        dR.save(d);
    }

    @Override
    public List<Diagnostico> list() {
        return dR.findAll();
    }

    @Override
    public Optional<Diagnostico> listId(int id) {
        return dR.findById(id);
    }
}
