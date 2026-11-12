package pe.edu.upeu.mana.service;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.env.MockEnvironment;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.entity.Comprobante;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.ComprobantePayloadMapper;
import pe.edu.upeu.mana.repository.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CorreoComprobanteTest {
    ComprobanteRepository comprobantes = mock(ComprobanteRepository.class);
    ApisPeruFacturacionCliente cliente = mock(ApisPeruFacturacionCliente.class);
    JavaMailSender sender = mock(JavaMailSender.class);
    @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
    Clock clock = Clock.fixed(Instant.parse("2026-11-10T15:00:00Z"), ZoneId.of("America/Lima"));
    CorreoComprobante correo;
    Comprobante c;

    @BeforeEach void preparar() {
        var config = new FacturacionProperties();
        var e = config.getEmpresa(); e.setRuc("20471380904"); e.setRazonSocial("MANA RESTAURANTE SAC"); e.setNombreComercial("MANÁ"); e.setDireccion("Ñaña");
        when(proveedor.getIfAvailable()).thenReturn(sender);
        when(sender.createMimeMessage()).thenAnswer(a -> new MimeMessage((Session) null));
        when(cliente.pdf(anyBoolean(), any())).thenReturn(new byte[] {37, 80, 68, 70});
        correo = new CorreoComprobante(comprobantes, mock(ComprobanteDetalleRepository.class), cliente, mock(ComprobantePayloadMapper.class), config, proveedor,
            new MockEnvironment().withProperty("spring.mail.username", "mana@gmail.com"), clock);
        c = new Comprobante(); c.setId(1L); c.setTipo(TipoComprobante.BOLETA); c.setSerie("B001"); c.setNumero(7); c.setEstado(EstadoComprobante.ACEPTADO);
        c.setAmbiente(AmbienteFacturacion.BETA); c.setReceptorNombre("ANA PEREZ"); c.setTotal(new BigDecimal("11.00")); c.setXml("<Invoice/>");
        c.setReceptorEmail("ana@correo.com");
        when(comprobantes.findById(1L)).thenReturn(Optional.of(c));
    }

    @Test void aceptadoConCorreoEnviaPdfYXmlUnaSolaVez() throws Exception {
        correo.enviarSiCorresponde(1L);
        var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender).send(captor.capture());
        var m = captor.getValue();
        assertEquals("ana@correo.com", m.getAllRecipients()[0].toString());
        assertTrue(m.getSubject().startsWith("[PRUEBA] Boleta de venta electrónica B001-00000007"), m.getSubject());
        var adjuntos = new ArrayList<String>();
        recorrer(m.getContent(), adjuntos);
        assertTrue(adjuntos.containsAll(List.of("B001-00000007.pdf", "20471380904-03-B001-7.xml")), adjuntos.toString());
        assertEquals(LocalDateTime.now(clock), c.getCorreoEnviadoEn());

        correo.enviarSiCorresponde(1L);
        verify(sender, times(1)).send(any(MimeMessage.class));
    }

    @Test void sinCorreoOPendienteNoEnvia() {
        c.setReceptorEmail(null); correo.enviarSiCorresponde(1L);
        c.setReceptorEmail("ana@correo.com"); c.setEstado(EstadoComprobante.PENDIENTE); correo.enviarSiCorresponde(1L);
        verify(sender, never()).send(any(MimeMessage.class));
        assertNull(c.getCorreoEnviadoEn());
    }

    @Test void falloSmtpAutomaticoNoLanzaYQuedaSinMarcar() {
        doThrow(new MailSendException("caido")).when(sender).send(any(MimeMessage.class));
        assertDoesNotThrow(() -> correo.enviarSiCorresponde(1L));
        assertNull(c.getCorreoEnviadoEn());
    }

    @Test void manualGuardaElNuevoCorreo() {
        correo.enviarAhora(c, " Otro@Correo.com ");
        assertEquals("otro@correo.com", c.getReceptorEmail());
        assertNotNull(c.getCorreoEnviadoEn());
    }

    @Test void manualExigeAceptadoCorreoYSmtp() {
        c.setEstado(EstadoComprobante.ERROR);
        assertThrows(ReglaNegocioException.class, () -> correo.enviarAhora(c, null));
        c.setEstado(EstadoComprobante.OBSERVADO); c.setReceptorEmail(null);
        assertThrows(ReglaNegocioException.class, () -> correo.enviarAhora(c, ""));
        when(proveedor.getIfAvailable()).thenReturn(null);
        assertThrows(ServicioExternoException.class, () -> correo.enviarAhora(c, "ana@correo.com"));
    }

    private static void recorrer(Object contenido, List<String> adjuntos) throws Exception {
        if (contenido instanceof Multipart mp) {
            for (int i = 0; i < mp.getCount(); i++) {
                var parte = mp.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(parte.getDisposition())) adjuntos.add(parte.getFileName());
                else recorrer(parte.getContent(), adjuntos);
            }
        }
    }
}
