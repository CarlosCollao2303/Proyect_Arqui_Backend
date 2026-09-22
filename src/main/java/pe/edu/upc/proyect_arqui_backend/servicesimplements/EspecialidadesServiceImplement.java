package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;
import pe.edu.upc.proyect_arqui_backend.repositories.IEspecialidadesRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IEspecialidadesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadesServiceImplement implements IEspecialidadesService {
    private final IEspecialidadesRepository eR;

    public EspecialidadesServiceImplement(IEspecialidadesRepository eR) {
        this.eR = eR;
    }

    @Override
    public void insert(Especialidades e) {
        eR.save(e);
    }

    @Override
    public void delete(int id) {
        eR.deleteById(id);
    }

    @Override
    public void update(Especialidades e) {
        eR.save(e);
    }

    @Override
    public List<Especialidades> list() {
        return eR.findAll();
    }

    @Override
    public Optional<Especialidades> listId(int id) {
        return eR.findById(id);
    }
}
