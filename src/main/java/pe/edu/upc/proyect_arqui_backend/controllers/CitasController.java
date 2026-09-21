package pe.edu.upc.proyect_arqui_backend.controllers;

import pe.edu.upc.proyect_arqui_backend.dtos.CitasDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICitasService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IUsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
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

    @PostMapping("/Registrar")
    public ResponseEntity<CitasDTO> registrar(@RequestBody CitasDTO dto) {
        Usuarios paciente = uS.listId(dto.getIdPaciente())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el paciente con el id: " + dto.getIdPaciente()
                        )
                );

        Usuarios medico = uS.listId(dto.getIdMedico())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el medico con el id: " + dto.getIdMedico()
                        )
                );

        Citas cita = new Citas();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHoraProgramada(dto.getFechaHoraProgramada());
        cita.setTiempoEsperaMinutos(dto.getTiempoEsperaMinutos());
        cita.setEstado(dto.getEstado());

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
    public ResponseEntity<CitasDTO> actualizar(@RequestBody CitasDTO dto) {
        Optional<Citas> existente = cS.listId(dto.getIdCita());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una cita con el id: " + dto.getIdCita()
            );
        }

        Usuarios paciente = uS.listId(dto.getIdPaciente())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el paciente con el id: " + dto.getIdPaciente()
                        )
                );

        Usuarios medico = uS.listId(dto.getIdMedico())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el medico con el id: " + dto.getIdMedico()
                        )
                );

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
