package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.repositories.IUsuariosRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuariosServiceImplement implements IUsuariosService {

    private final IUsuariosRepository uR;

    public UsuariosServiceImplement(IUsuariosRepository uR) {
        this.uR = uR;
    }

    @Override
    public void insert(Usuarios u) {
        uR.save(u);
    }

    @Override
    public void delete(int id) {
        uR.deleteById(id);
    }

    @Override
    public void update(Usuarios u) {
        uR.save(u);
    }

    @Override
    public List<Usuarios> list() {
        return uR.findAll();
    }

    @Override
    public Optional<Usuarios> listId(int id) {
        return uR.findById(id);
    }

    @Override
    public Optional<Usuarios> listByCorreo(String correo) {
        return uR.findByCorreo(correo);
    }
}
