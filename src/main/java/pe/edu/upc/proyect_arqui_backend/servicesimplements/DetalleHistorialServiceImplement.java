package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.repositories.IDetalleHistorialRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetalleHistorialServiceImplement implements IDetalleHistorialService {

    private final IDetalleHistorialRepository dhR;

    public DetalleHistorialServiceImplement(IDetalleHistorialRepository dhR) {
        this.dhR = dhR;
    }

    @Override
    public void insert(DetalleHistorial d) {
        dhR.save(d);
    }

    @Override
    public void delete(int id) {
        dhR.deleteById(id);
    }

    @Override
    public void update(DetalleHistorial d) {
        dhR.save(d);
    }

    @Override
    public List<DetalleHistorial> list() {
        return dhR.findAll();
    }

    @Override
    public Optional<DetalleHistorial> listId(int id) {
        return dhR.findById(id);
    }

    @Override
    public Optional<DetalleHistorial> findByExamenId(int idExamen) {
        return dhR.findByExamen_IdExamen(idExamen);
    }

    @Override
    public Optional<DetalleHistorial> findByDiagnosticoId(int idDiagnostico) {
        return dhR.findByDiagnostico_IdDiagnostico(idDiagnostico);
    }

    @Override
    public Optional<DetalleHistorial> findByTratamientoId(int idTratamiento) {
        return dhR.findByTratamiento_IdTratamiento(idTratamiento);
    }

    @Override
    public Optional<DetalleHistorial> findByRecetaId(int idReceta) {
        return dhR.findByReceta_IdReceta(idReceta);
    }
}
