package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.proyect_arqui_backend.dtos.MedicoPorEspecialidadDTO;
import pe.edu.upc.proyect_arqui_backend.exceptions.ResourceNotFoundException;
import pe.edu.upc.proyect_arqui_backend.repositories.IEspecialidadesRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EspecialidadesServiceImplementTest {

    @Mock
    private IEspecialidadesRepository eR;

    @InjectMocks
    private EspecialidadesServiceImplement eS;

    @Test
    void listMedicosByEspecialidadId_devuelveLosMedicos() {
        List<MedicoPorEspecialidadDTO> medicos = List.of(
                new MedicoPorEspecialidadDTO(7, "Luis", "Rojas", "Cardiologia")
        );
        when(eR.existsById(1)).thenReturn(true);
        when(eR.findMedicosByEspecialidadId(1)).thenReturn(medicos);

        assertEquals(medicos, eS.listMedicosByEspecialidadId(1));
    }

    @Test
    void listMedicosByEspecialidadId_especialidadSinMedicosDevuelveListaVacia() {
        when(eR.existsById(2)).thenReturn(true);
        when(eR.findMedicosByEspecialidadId(2)).thenReturn(List.of());

        assertTrue(eS.listMedicosByEspecialidadId(2).isEmpty());
    }

    @Test
    void listMedicosByEspecialidadId_especialidadInexistenteLanza404() {
        when(eR.existsById(99)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> eS.listMedicosByEspecialidadId(99));

        assertEquals("La especialidad con id 99 no existe", ex.getMessage());
        verify(eR, never()).findMedicosByEspecialidadId(anyInt());
    }
}
