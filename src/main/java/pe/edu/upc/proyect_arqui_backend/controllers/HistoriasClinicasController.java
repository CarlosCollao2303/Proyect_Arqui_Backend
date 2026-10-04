package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.CantidadDiagnosticosDTO;
import pe.edu.upc.proyect_arqui_backend.dtos.HistoriasClinicasDTO;
import pe.edu.upc.proyect_arqui_backend.entities.HistoriasClinicas;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IHistoriasClinicasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAnyAuthority('ADMIN','MEDICO')")
@RequestMapping("/historiasclinicas")
public class HistoriasClinicasController {

    private final IHistoriasClinicasService hS;
    private final IUsuariosService uS;

    public HistoriasClinicasController(IHistoriasClinicasService hS, IUsuariosService uS) {
        this.hS = hS;
        this.uS = uS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<HistoriasClinicasDTO>> listar() {
        List<HistoriasClinicasDTO> lista = hS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<HistoriasClinicasDTO> listarPorId(@PathVariable int id) {
        HistoriasClinicas historiaClinica = hS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una historia clinica con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(historiaClinica));
    }

    @GetMapping("/ListarPorPaciente/{idPaciente}")
    public ResponseEntity<HistoriasClinicasDTO> listarPorPaciente(@PathVariable int idPaciente) {
        HistoriasClinicas historiaClinica = hS.listByPacienteId(idPaciente)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una historia clinica para el paciente con id: " + idPaciente
                        )
                );

        return ResponseEntity.ok(convertirADTO(historiaClinica));
    }
    @GetMapping("/cantidad-diagnosticos")
    public ResponseEntity<List<CantidadDiagnosticosDTO>> cantidadDiagnosticos() {
        return ResponseEntity.ok(hS.contarDiagnosticosPorHistoria());
    }

    @GetMapping("/{idHistoria}/cantidad-diagnosticos")
    public ResponseEntity<CantidadDiagnosticosDTO> cantidadDiagnosticosDeHistoria(@PathVariable int idHistoria) {
        return ResponseEntity.ok(hS.contarDiagnosticosDeHistoria(idHistoria));
    }
    @PostMapping("/Registrar")
    public ResponseEntity<HistoriasClinicasDTO> registrar(@Valid @RequestBody HistoriasClinicasDTO dto) {
        Usuarios paciente = obtenerPaciente(dto.getIdPaciente());

        HistoriasClinicas historiaClinica = new HistoriasClinicas();
        historiaClinica.setPaciente(paciente);
        historiaClinica.setFechaCreacion(dto.getFechaCreacion());

        hS.insert(historiaClinica);

        HistoriasClinicasDTO responseDTO = convertirADTO(historiaClinica);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(historiaClinica.getIdHistoriaClinica())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    public ResponseEntity<HistoriasClinicasDTO> actualizar(@Valid @RequestBody HistoriasClinicasDTO dto) {
        Optional<HistoriasClinicas> existente = hS.listId(dto.getIdHistoriaClinica());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una historia clinica con el id: " + dto.getIdHistoriaClinica()
            );
        }

        Usuarios paciente = obtenerPaciente(dto.getIdPaciente());

        HistoriasClinicas historiaClinica = existente.get();
        historiaClinica.setPaciente(paciente);
        historiaClinica.setFechaCreacion(dto.getFechaCreacion());

        hS.update(historiaClinica);

        HistoriasClinicasDTO responseDTO = convertirADTO(historiaClinica);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        HistoriasClinicas historiaClinica = hS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una historia clinica con el id: " + id
                        )
                );

        hS.delete(historiaClinica.getIdHistoriaClinica());
        return ResponseEntity.noContent().build();
    }

    private HistoriasClinicasDTO convertirADTO(HistoriasClinicas historiaClinica) {
        HistoriasClinicasDTO dto = new HistoriasClinicasDTO();
        dto.setIdHistoriaClinica(historiaClinica.getIdHistoriaClinica());
        dto.setIdPaciente(historiaClinica.getPaciente().getIdUsuario());
        dto.setFechaCreacion(historiaClinica.getFechaCreacion());
        return dto;
    }

    private Usuarios obtenerPaciente(Integer idPaciente) {
        Usuarios paciente = uS.listId(idPaciente)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el usuario con el id: " + idPaciente
                        )
                );

        if (paciente.getRol() == null || !"PACIENTE".equalsIgnoreCase(paciente.getRol().getNombre())) {
            throw new BadRequestException(
                    "El usuario con id " + idPaciente + " no tiene el rol PACIENTE; solo los pacientes pueden tener historia clinica"
            );
        }

        return paciente;
    }
}
