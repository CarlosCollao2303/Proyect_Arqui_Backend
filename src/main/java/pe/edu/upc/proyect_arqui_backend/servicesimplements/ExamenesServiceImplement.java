package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.dtos.CantidadExamenesDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.ExamenPendienteDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
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

    @Override
    public List<Examenes> listByTipoExamen(String tipoExamen) {
        return eR.findByTipoExamenContainingIgnoreCase(tipoExamen);
    }

    @Override
    public List<CantidadExamenesDTO> contarExamenesPorPaciente() {
        return eR.contarExamenesPorPaciente();
    }

    @Override
    public CantidadExamenesDTO contarExamenesDePaciente(int idPaciente) {
        return eR.contarExamenesDePaciente(idPaciente)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un paciente con el id: " + idPaciente
                        )
                );
    }

    @Override
    public List<ExamenPendienteDTO> listarExamenesPendientes() {
        return eR.listarExamenesPendientes();
    }
}
