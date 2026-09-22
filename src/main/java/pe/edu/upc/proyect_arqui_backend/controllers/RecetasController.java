package pe.edu.upc.proyect_arqui_backend.controllers;

import jakarta.validation.Valid;
import pe.edu.upc.proyect_arqui_backend.dtos.RecetasDTO;
import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;
import pe.edu.upc.proyect_arqui_backend.entities.Recetas;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IDetalleHistorialService;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.IRecetasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recetas")
public class RecetasController {

    private final IRecetasService rS;
    private final IDetalleHistorialService dhS;

    public RecetasController(IRecetasService rS, IDetalleHistorialService dhS) {
        this.rS = rS;
        this.dhS = dhS;
    }

    @GetMapping("/Listar")
    public ResponseEntity<List<RecetasDTO>> listar() {
        List<RecetasDTO> lista = rS.list()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PostMapping("/Registrar")
    public ResponseEntity<RecetasDTO> registrar(@Valid @RequestBody RecetasDTO dto) {
        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Recetas receta = new Recetas();
        receta.setDetalleHistorial(detalle);
        receta.setMedicamento(dto.getMedicamento());
        receta.setDosis(dto.getDosis());
        receta.setFrecuencia(dto.getFrecuencia());
        receta.setDuracionDias(dto.getDuracionDias());
        receta.setIndicaciones(dto.getIndicaciones());
        receta.setFechaEmision(dto.getFechaEmision());

        rS.insert(receta);

        RecetasDTO responseDTO = convertirADTO(receta);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(receta.getIdReceta())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping("/Actualizar")
    public ResponseEntity<RecetasDTO> actualizar(@Valid @RequestBody RecetasDTO dto) {
        Optional<Recetas> existente = rS.listId(dto.getIdReceta());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una receta con el id: " + dto.getIdReceta()
            );
        }

        DetalleHistorial detalle = obtenerDetalle(dto.getIdDetalleHistorial());

        Recetas receta = existente.get();
        receta.setDetalleHistorial(detalle);
        receta.setMedicamento(dto.getMedicamento());
        receta.setDosis(dto.getDosis());
        receta.setFrecuencia(dto.getFrecuencia());
        receta.setDuracionDias(dto.getDuracionDias());
        receta.setIndicaciones(dto.getIndicaciones());
        receta.setFechaEmision(dto.getFechaEmision());

        rS.update(receta);

        RecetasDTO responseDTO = convertirADTO(receta);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        Recetas receta = rS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una receta con el id: " + id
                        )
                );

        rS.delete(receta.getIdReceta());
        return ResponseEntity.noContent().build();
    }

    private DetalleHistorial obtenerDetalle(Integer idDetalleHistorial) {
        return dhS.listId(idDetalleHistorial)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el detalle de historial con el id: " + idDetalleHistorial
                        )
                );
    }

    private RecetasDTO convertirADTO(Recetas receta) {
        RecetasDTO dto = new RecetasDTO();
        dto.setIdReceta(receta.getIdReceta());
        dto.setIdDetalleHistorial(receta.getDetalleHistorial().getIdDetalleHistorial());
        dto.setMedicamento(receta.getMedicamento());
        dto.setDosis(receta.getDosis());
        dto.setFrecuencia(receta.getFrecuencia());
        dto.setDuracionDias(receta.getDuracionDias());
        dto.setIndicaciones(receta.getIndicaciones());
        dto.setFechaEmision(receta.getFechaEmision());
        return dto;
    }
}
