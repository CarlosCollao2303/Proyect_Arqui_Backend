package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.repositories.ICitasRepository;
import pe.edu.upc.proyect_arqui_backend.repositories.IUsuariosRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitasServiceImplement implements ICitasService {

    private final ICitasRepository cR;
    private final IUsuariosRepository uR;

    public CitasServiceImplement(ICitasRepository cR, IUsuariosRepository uR) {
        this.cR = cR;
        this.uR = uR;
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

    @Override
    public List<CitaMedicoDTO> listarCitasDelMedico(String correoMedico, String estado,
                                                    LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        Usuarios medico = uR.findByCorreo(correoMedico)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el usuario con el correo: " + correoMedico
                        )
                );

        String estadoFiltro = estado == null || estado.isBlank()
                ? Citas.ESTADO_PENDIENTE
                : estado.trim().toUpperCase();
        if (!Citas.ESTADOS.contains(estadoFiltro)) {
            throw new BadRequestException("El estado de la cita debe ser uno de: " + Citas.ESTADOS);
        }

        LocalDateTime desde = inicio != null ? inicio.atStartOfDay() : null;
        LocalDateTime hasta = fin != null ? fin.plusDays(1).atStartOfDay() : null;

        List<CitaMedicoDTO> citas = cR.findCitasDelMedico(medico.getIdUsuario(), estadoFiltro, desde, hasta);
        if (citas.isEmpty()) {
            throw new ResourceNotFoundException("No tienes citas programadas");
        }
        return citas;
    }
}
