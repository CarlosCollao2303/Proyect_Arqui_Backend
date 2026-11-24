package pe.edu.upc.proyect_arqui_backend.servicesimplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.proyect_arqui_backend.dtos.MensajeCitaDTO;
import pe.edu.upc.proyect_arqui_backend.entities.Citas;
import pe.edu.upc.proyect_arqui_backend.entities.Especialidades;
import pe.edu.upc.proyect_arqui_backend.entities.Notificaciones;
import pe.edu.upc.proyect_arqui_backend.entities.Roles;
import pe.edu.upc.proyect_arqui_backend.entities.Usuarios;
import pe.edu.upc.proyect_arqui_backend.exceptions.CorreoException;
import pe.edu.upc.proyect_arqui_backend.repositories.INotificacionesRepository;
import pe.edu.upc.proyect_arqui_backend.servicesinterfaces.ICorreoService;

import java.time.LocalDateTime;

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

    // ---- Mensajes pre armados de las citas ----

    private Citas cita(String estado, int minutosEspera) {
        Usuarios luis = new Usuarios(2, new Roles(2, "MEDICO"), new Especialidades(1, "Cardiologia"),
                "Luis", "Rojas", "87654321", "988888888", "luis@clinica.pe", "hash", null, null, true);
        return new Citas(16, ana, luis, LocalDateTime.of(2026, 11, 24, 0, 0), minutosEspera, estado);
    }

    @Test
    void armarMensajeCita_pendiente_esRecordatorioConLosDatosDeLaCita() {
        MensajeCitaDTO mensaje = service.armarMensajeCita(cita("PENDIENTE", 0), null);

        assertEquals("RECORDATORIO", mensaje.tipo());
        assertEquals("Ana Torres", mensaje.destinatario());
        assertEquals("ana@mail.com", mensaje.correo());
        assertTrue(mensaje.asunto().contains("24/11/2026"));
        assertTrue(mensaje.mensaje().contains("#16"));
        assertTrue(mensaje.mensaje().contains("Luis Rojas (Cardiologia)"));
        assertTrue(mensaje.mensaje().contains("24/11/2026"));
    }

    @Test
    void armarMensajeCita_atendida_incluyeElTiempoDeEspera() {
        MensajeCitaDTO mensaje = service.armarMensajeCita(cita("ATENDIDA", 15), null);

        assertEquals("CITA_ATENDIDA", mensaje.tipo());
        assertTrue(mensaje.mensaje().contains("15 minutos"));
    }

    @Test
    void armarMensajeCita_cancelada() {
        MensajeCitaDTO mensaje = service.armarMensajeCita(cita("CANCELADA", 0), null);

        assertEquals("CITA_CANCELADA", mensaje.tipo());
        assertTrue(mensaje.asunto().contains("cancelada"));
    }

    @Test
    void armarMensajeCita_tipoExplicito_reprogramada() {
        MensajeCitaDTO mensaje = service.armarMensajeCita(cita("PENDIENTE", 0), "CITA_REPROGRAMADA");

        assertEquals("CITA_REPROGRAMADA", mensaje.tipo());
        assertTrue(mensaje.mensaje().contains("reprogramada para el 24/11/2026"));
    }

    @Test
    void notificarCita_pendiente_mandaElBotonDeVideollamada() {
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        when(correoService.enviar(eq("ana@mail.com"), anyString(), html.capture())).thenReturn("id-resend");

        Notificaciones n = service.notificarCita(cita("PENDIENTE", 0), null, "https://meet.jit.si/SaludCerca-abc");

        assertEquals("RECORDATORIO", n.getTipo());
        assertTrue(html.getValue().contains("href=\"https://meet.jit.si/SaludCerca-abc\""));
    }

    @Test
    void notificarCita_cancelada_noMandaElBotonDeVideollamada() {
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        when(correoService.enviar(eq("ana@mail.com"), anyString(), html.capture())).thenReturn("id-resend");

        service.notificarCita(cita("CANCELADA", 0), null, "https://meet.jit.si/SaludCerca-abc");

        assertFalse(html.getValue().contains("meet.jit.si"));
    }
}
