package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import pe.edu.upc.proyect_arqui_backend.dtos.CantidadDiagnosticosDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.ConsultaHistorialDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.DiagnosticoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.ExamenesDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.HistorialClinicoDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.RecetasDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.TratamientosDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Diagnostico;
import pe.edu.upc.proyect_arqui_backend.entities.Examenes;
import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;
import pe.edu.upc.proyect_arqui_backend.entities.Recetas;
import pe.edu.upc.proyect_arqui_backend.entities.Tratamientos;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.repositories.ICitasRepository;
import pe.edu.upc.proyect_arqui_backend.repositories.IDetalleHistorialRepository;
import pe.edu.upc.proyect_arqui_backend.repositories.IHistoriasClinicasRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IHistoriasClinicasService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Service
public class HistoriasClinicasServiceImplement implements IHistoriasClinicasService {

    private final IHistoriasClinicasRepository hR;
    private final IDetalleHistorialRepository dR;
    private final ICitasRepository cR;

    public HistoriasClinicasServiceImplement(IHistoriasClinicasRepository hR,
                                             IDetalleHistorialRepository dR,
                                             ICitasRepository cR) {
        this.hR = hR;
        this.dR = dR;
        this.cR = cR;
    }

    @Override
    public void insert(HistoriasClinicas h) {
        hR.save(h);
    }

    @Override
    public void delete(int id) {
        hR.deleteById(id);
    }

    @Override
    public void update(HistoriasClinicas h) {
        hR.save(h);
    }

    @Override
    public List<HistoriasClinicas> list() {
        return hR.findAll();
    }

    @Override
    public Optional<HistoriasClinicas> listId(int id) {
        return hR.findById(id);
    }

    @Override
    public Optional<HistoriasClinicas> listByPacienteId(int idPaciente) {
        return hR.findByPacienteIdNative(idPaciente);
    }
    @Override
    public List<CantidadDiagnosticosDTO> contarDiagnosticosPorHistoria() {
        return hR.contarDiagnosticosPorHistoria();
    }

    @Override
    public CantidadDiagnosticosDTO contarDiagnosticosDeHistoria(int idHistoria) {
        return hR.contarDiagnosticosDeHistoria(idHistoria)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una historia clinica con el id: " + idHistoria
                        )
                );
    }

    // Junta en un solo objeto todo lo que hay en la historia: las citas del paciente y lo que
    // los medicos registraron en cada detalle. Con vistaPaciente solo se llenan citas, examenes
    // y recetas: el filtro se hace aqui, en el back, y no solo escondiendolo en el front.
    @Override
    public HistorialClinicoDTO armarHistorial(HistoriasClinicas historia, boolean vistaPaciente) {
        Usuarios paciente = historia.getPaciente();
        List<DetalleHistorial> detalles = dR
                .findAllByHistoriaClinica_IdHistoriaClinicaOrderByFechaRegistroDesc(historia.getIdHistoriaClinica());

        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        dto.setIdHistoriaClinica(historia.getIdHistoriaClinica());
        dto.setPaciente(paciente.getNombres() + " " + paciente.getApellidos());
        dto.setDni(paciente.getDni());
        dto.setFechaCreacion(historia.getFechaCreacion() == null
                ? null : historia.getFechaCreacion().toLocalDate());

        dto.setCitas(cR.findEstadosDelPaciente(paciente.getIdUsuario()));
        dto.setExamenes(distintos(detalles, DetalleHistorial::getExamen, Examenes::getIdExamen)
                .stream().map(this::convertirExamen).toList());
        dto.setRecetas(distintos(detalles, DetalleHistorial::getReceta, Recetas::getIdReceta)
                .stream().map(this::convertirReceta).toList());

        if (!vistaPaciente) {
            dto.setConsultas(detalles.stream().map(this::convertirConsulta).toList());
            dto.setDiagnosticos(distintos(detalles, DetalleHistorial::getDiagnostico, Diagnostico::getIdDiagnostico)
                    .stream().map(this::convertirDiagnostico).toList());
            dto.setTratamientos(distintos(detalles, DetalleHistorial::getTratamiento, Tratamientos::getIdTratamiento)
                    .stream().map(this::convertirTratamiento).toList());
        }

        return dto;
    }

    // Un mismo examen (o receta, etc.) puede estar en varios detalles: se muestra una sola vez.
    private <T> List<T> distintos(List<DetalleHistorial> detalles, Function<DetalleHistorial, T> campo,
                                  Function<T, Integer> id) {
        Set<Integer> vistos = new HashSet<>();
        return detalles.stream()
                .map(campo)
                .filter(Objects::nonNull)
                .filter(item -> vistos.add(id.apply(item)))
                .toList();
    }

    private ConsultaHistorialDTO convertirConsulta(DetalleHistorial detalle) {
        Usuarios medico = detalle.getMedico();
        return new ConsultaHistorialDTO(
                detalle.getIdDetalleHistorial(),
                detalle.getFechaRegistro() == null ? null : detalle.getFechaRegistro().toLocalDate(),
                medico.getNombres() + " " + medico.getApellidos(),
                detalle.getMotivoConsulta()
        );
    }

    private ExamenesDTO convertirExamen(Examenes examen) {
        ExamenesDTO dto = new ExamenesDTO();
        dto.setIdExamen(examen.getIdExamen());
        dto.setTipoExamen(examen.getTipoExamen());
        dto.setDescripcion(examen.getDescripcion());
        dto.setFechaSolicitud(examen.getFechaSolicitud().toLocalDate());
        dto.setFechaResultado(examen.getFechaResultado() == null
                ? null : examen.getFechaResultado().toLocalDate());
        return dto;
    }

    private RecetasDTO convertirReceta(Recetas receta) {
        RecetasDTO dto = new RecetasDTO();
        dto.setIdReceta(receta.getIdReceta());
        dto.setMedicamento(receta.getMedicamento());
        dto.setDosis(receta.getDosis());
        dto.setFrecuencia(receta.getFrecuencia());
        dto.setDuracionDias(receta.getDuracionDias());
        dto.setIndicaciones(receta.getIndicaciones());
        dto.setFechaEmision(receta.getFechaEmision().toLocalDate());
        return dto;
    }

    private DiagnosticoDTO convertirDiagnostico(Diagnostico diagnostico) {
        DiagnosticoDTO dto = new DiagnosticoDTO();
        dto.setIdDiagnostico(diagnostico.getIdDiagnostico());
        dto.setCodigoCie10(diagnostico.getCodigoCie10());
        dto.setDescripcion(diagnostico.getDescripcion());
        dto.setFechaRegistro(diagnostico.getFechaRegistro().toLocalDate());
        return dto;
    }

    private TratamientosDTO convertirTratamiento(Tratamientos tratamiento) {
        TratamientosDTO dto = new TratamientosDTO();
        dto.setIdTratamiento(tratamiento.getIdTratamiento());
        dto.setDescripcion(tratamiento.getDescripcion());
        dto.setFechaInicio(tratamiento.getFechaInicio());
        dto.setFechaFin(tratamiento.getFechaFin());
        dto.setEstado(tratamiento.getEstado());
        return dto;
    }
}
