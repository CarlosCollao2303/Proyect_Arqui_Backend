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
    public List<Roles> list() {
        return rR.findAll();
    }

    @Override
    public Optional<Roles> listId(int id) {
        return rR.findById(id);
    }
}
