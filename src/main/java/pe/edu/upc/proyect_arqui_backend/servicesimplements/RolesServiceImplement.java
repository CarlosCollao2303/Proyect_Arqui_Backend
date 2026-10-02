package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.repositories.IRolesRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRolesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolesServiceImplement implements IRolesService {

    private final IRolesRepository rR;

    public RolesServiceImplement(IRolesRepository rR) {
        this.rR = rR;
    }

    @Override
    public void insert(Roles r) {
        rR.save(r);
    }

    @Override
    public void delete(int id) {
        rR.deleteById(id);
    }

    @Override
    public void update(Roles r) {
        rR.save(r);
    }

    @Override
    public List<Roles> list() {
        return rR.findAll();
    }

    @Override
    public Optional<Roles> listId(int id) {
        return rR.findById(id);
    }

    @Override
    public Optional<Roles> listByNombre(String nombre) {
        return rR.findByNombre(nombre);
    }
}
