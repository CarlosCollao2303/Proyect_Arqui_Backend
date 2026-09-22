package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.CitasDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.BadRequestException;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAnyAuthority('ADMIN','MEDICO')")
@RequestMapping("/citas")
public class CitasController {

    private final ICitasService cS;
    private final IUsuariosService uS;

    public CitasController(ICitasService cS, IUsuariosService uS) {
        this.cS = cS;
        this.uS = uS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<CitasDTO>> listar() {
        List<CitasDTO> lista = cS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Las citas del usuario logueado: como paciente o como medico. El correo sale
    // del token (subject del JWT), no del request, asi nadie puede pedir las de otro.
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mis-citas")
    public ResponseEntity<List<CitasDTO>> misCitas(Authentication auth) {
        List<CitasDTO> lista = cS.listByParticipanteCorreo(auth.getName())
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/ListarPorId/{id}")
    public ResponseEntity<CitasDTO> listarPorId(@PathVariable int id) {
        Citas cita = cS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una cita con el id: " + id
                        )
                );

        return ResponseEntity.ok(convertirADTO(cita));
    }

    // Un PACIENTE solo reserva para si mismo: el paciente sale del token y se ignora
    // el idPaciente del body. Ademas la cita nace PENDIENTE y sin tiempo de espera;
    // esos campos los maneja el personal. ADMIN y MEDICO pueden registrar para cualquiera.
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDICO','PACIENTE')")
    @PostMapping("/Registrar")
    public ResponseEntity<CitasDTO> registrar(@Valid @RequestBody CitasDTO dto, Authentication auth) {
        boolean esPaciente = !tieneRol(auth, "ADMIN") && !tieneRol(auth, "MEDICO");

        Usuarios paciente;
        if (esPaciente) {
            paciente = uS.listByCorreo(auth.getName())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "No existe el usuario con el correo: " + auth.getName()
                            )
                    );
        } else {
            paciente = obtenerPaciente(dto.getIdPaciente());
        }

        Usuarios medico = obtenerMedico(dto.getIdMedico());

        Citas cita = new Citas();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHoraProgramada(dto.getFechaHoraProgramada());
        cita.setTiempoEsperaMinutos(esPaciente ? 0 : dto.getTiempoEsperaMinutos());
        cita.setEstado(esPaciente ? "PENDIENTE" : dto.getEstado());

        cS.insert(cita);

        CitasDTO responseDTO = convertirADTO(cita);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cita.getIdCita())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    public ResponseEntity<CitasDTO> actualizar(@Valid @RequestBody CitasDTO dto) {
        Optional<Citas> existente = cS.listId(dto.getIdCita());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una cita con el id: " + dto.getIdCita()
            );
        }

        Usuarios paciente = obtenerPaciente(dto.getIdPaciente());
        Usuarios medico = obtenerMedico(dto.getIdMedico());

        Citas cita = existente.get();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHoraProgramada(dto.getFechaHoraProgramada());
        cita.setTiempoEsperaMinutos(dto.getTiempoEsperaMinutos());
        cita.setEstado(dto.getEstado());

        cS.update(cita);

        CitasDTO responseDTO = convertirADTO(cita);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Citas cita = cS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una cita con el id: " + id
                        )
                );

        cS.delete(cita.getIdCita());
        return ResponseEntity.noContent().build();
    }

    private Usuarios obtenerPaciente(Integer idPaciente) {
        if (idPaciente == null) {
            throw new BadRequestException("El id del paciente es obligatorio");
        }

        return uS.listId(idPaciente)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el paciente con el id: " + idPaciente
                        )
                );
    }

    // Ademas de existir, el usuario tiene que tener rol MEDICO: si no, se podria
    // agendar una cita "con" un paciente o un admin.
    private Usuarios obtenerMedico(Integer idMedico) {
        Usuarios medico = uS.listId(idMedico)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el medico con el id: " + idMedico
                        )
                );

        if (!"MEDICO".equals(medico.getRol().getNombre())) {
            throw new BadRequestException("El usuario con id " + idMedico + " no es medico");
        }

        return medico;
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }

    private CitasDTO convertirADTO(Citas cita) {
        CitasDTO dto = new CitasDTO();
        dto.setIdCita(cita.getIdCita());
        dto.setIdPaciente(cita.getPaciente().getIdUsuario());
        dto.setIdMedico(cita.getMedico().getIdUsuario());
        dto.setFechaHoraProgramada(cita.getFechaHoraProgramada());
        dto.setTiempoEsperaMinutos(cita.getTiempoEsperaMinutos());
        dto.setEstado(cita.getEstado());
        return dto;
    }
}
