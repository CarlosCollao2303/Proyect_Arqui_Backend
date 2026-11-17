package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.CorreoException;
import pe.edu.upc.proyect_arqui_backend.repositories.INotificacionesRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICorreoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionesServiceImplementTest {

    @Mock
    private INotificacionesRepository nR;

    @Mock
    private ICorreoService correoService;

    @InjectMocks
    private NotificacionesServiceImplement service;

    private final Usuarios ana = new Usuarios(1, new Roles(3, "PACIENTE"), null, "Ana", "Torres", "12345678",
            "999999999", "ana@mail.com", "hash", null, null, true);

    @Test
    void enviarPorCorreo_correoEnviado_guardaComoEnviado() {
        when(correoService.enviar(eq("ana@mail.com"), eq("Asunto"), anyString())).thenReturn("id-resend");

        Notificaciones n = service.enviarPorCorreo(ana, "RECORDATORIO", "Asunto", "Hola", null);

        assertEquals(Notificaciones.ESTADO_ENVIADO, n.getEstadoEnvio());
        assertEquals("RECORDATORIO", n.getTipo());
        assertNotNull(n.getFechaEnvio());
        verify(nR).save(n);
    }

    @Test
    void enviarPorCorreo_resendFalla_guardaComoFallidoYRelanza() {
        when(correoService.enviar(anyString(), anyString(), anyString()))
                .thenThrow(new CorreoException("No se pudo enviar el correo"));

        assertThrows(CorreoException.class,
                () -> service.enviarPorCorreo(ana, "RECORDATORIO", "Asunto", "Hola", null));

        ArgumentCaptor<Notificaciones> guardada = ArgumentCaptor.forClass(Notificaciones.class);
        verify(nR).save(guardada.capture());
        assertEquals(Notificaciones.ESTADO_FALLIDO, guardada.getValue().getEstadoEnvio());
    }

    @Test
    void enviarPorCorreo_escapaElMensajeYAgregaElBotonDeVideollamada() {
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        when(correoService.enviar(anyString(), anyString(), html.capture())).thenReturn("id-resend");

        service.enviarPorCorreo(ana, "CITA_REGISTRADA", "Asunto",
                "<script>alert(1)</script>", "https://meet.jit.si/SaludCerca-abc");

        assertFalse(html.getValue().contains("<script>"));
        assertTrue(html.getValue().contains("&lt;script&gt;"));
        assertTrue(html.getValue().contains("href=\"https://meet.jit.si/SaludCerca-abc\""));
    }
}
