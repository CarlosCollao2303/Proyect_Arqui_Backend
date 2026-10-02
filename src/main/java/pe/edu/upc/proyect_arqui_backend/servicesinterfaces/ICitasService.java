package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.dtos.CitaEstadoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaMedicoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.CitaPacienteDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ICitasService {
    public void insert(Citas c);
    public void delete(int id);
    public void update(Citas c);
    public List<Citas> list();
    public Optional<Citas> listId(int id);
    public List<CitaPacienteDTO> listarCitasDelPacientePorFecha(String correoPaciente, LocalDate fecha);
    public List<Citas> listByParticipanteCorreo(String correo);
    public CitaEstadoDTO consultarEstado(int idCita, String correo, boolean puedeVerTodas);
    public List<CitaMedicoDTO> listarCitasDelMedico(String correoMedico, String estado, LocalDate inicio, LocalDate fin);
}
