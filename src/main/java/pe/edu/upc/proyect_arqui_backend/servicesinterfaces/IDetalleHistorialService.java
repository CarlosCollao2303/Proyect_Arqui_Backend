package pe.edu.upc.proyect_arqui_backend.servicesinterfaces;

import pe.edu.upc.proyect_arqui_backend.entities.DetalleHistorial;

import java.util.List;
import java.util.Optional;

public interface IDetalleHistorialService {
    public void insert(DetalleHistorial d);
    public void delete(int id);
    public void update(DetalleHistorial d);
    public List<DetalleHistorial> list();
    public Optional<DetalleHistorial> listId(int id);
    public List<DetalleHistorial> findAllByExamenId(int idExamen);
    public List<DetalleHistorial> findAllByDiagnosticoId(int idDiagnostico);
    public List<DetalleHistorial> findAllByTratamientoId(int idTratamiento);
    public List<DetalleHistorial> findAllByRecetaId(int idReceta);
}
