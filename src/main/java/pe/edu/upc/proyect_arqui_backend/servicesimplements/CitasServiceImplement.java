package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.repositories.ICitasRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CitasServiceImplement implements ICitasService {

    private final ICitasRepository cR;

    public CitasServiceImplement(ICitasRepository cR) {
        this.cR = cR;
    }

    @Override
    public void insert(Citas c) {
        cR.save(c);
    }

    @Override
    public void delete(int id) {
        cR.deleteById(id);
    }

    @Override
    public void update(Citas c) {
        cR.save(c);
    }

    @Override
    public List<Citas> list() {
        return cR.findAll();
    }

    @Override
    public Optional<Citas> listId(int id) {
        return cR.findById(id);
    }

    @Override
    public List<Citas> listByParticipanteCorreo(String correo) {
        return cR.findByParticipanteCorreo(correo);
    }
}
