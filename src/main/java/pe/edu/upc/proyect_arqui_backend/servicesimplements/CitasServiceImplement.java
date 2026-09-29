package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.repositories.ICitasRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import org.springframework.security.access.AccessDeniedException;
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

    // puedeVerTodas es true para ADMIN y MEDICO; un PACIENTE solo ve las citas en las
    // que es el paciente (correo = subject del JWT).
    @Override
    public CitaEstadoDTO consultarEstado(int idCita, String correo, boolean puedeVerTodas) {
        CitaEstadoDTO estado = cR.findEstadoById(idCita)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "La cita con id " + idCita + " no existe"
                        )
                );

        if (!puedeVerTodas && !cR.existsByIdCitaAndPacienteCorreo(idCita, correo)) {
            throw new AccessDeniedException("Solo puedes consultar el estado de tus propias citas");
        }

        return estado;
    }
}
