package pe.edu.upeu.mana.service;

import jakarta.mail.MessagingException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.entity.Comprobante;
import pe.edu.upeu.mana.enums.AmbienteFacturacion;
import pe.edu.upeu.mana.enums.EstadoComprobante;
import pe.edu.upeu.mana.enums.TipoComprobante;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.exception.RecursoNoEncontradoException;
import pe.edu.upeu.mana.exception.ServicioExternoException;
import pe.edu.upeu.mana.mapper.ComprobanteMapper;
import pe.edu.upeu.mana.mapper.ComprobantePayloadMapper;
import pe.edu.upeu.mana.repository.ComprobanteDetalleRepository;
import pe.edu.upeu.mana.repository.ComprobanteRepository;

// Envía al cliente el PDF (generado por APIsPERU) y el XML firmado de un comprobante aceptado por SUNAT.
// Usa el mismo SMTP que la recuperación de contraseña (spring.mail.*).
@Component
public class CorreoComprobante {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CorreoComprobante.class);

    private final ComprobanteRepository comprobantes;
    private final ComprobanteDetalleRepository detalles;
    private final ApisPeruFacturacionCliente cliente;
    private final ComprobantePayloadMapper payload;
    private final FacturacionProperties config;
    private final ObjectProvider<JavaMailSender> correo;
    private final Environment environment;
    private final Clock clock;

    public CorreoComprobante(ComprobanteRepository comprobantes, ComprobanteDetalleRepository detalles, ApisPeruFacturacionCliente cliente,
            ComprobantePayloadMapper payload, FacturacionProperties config, ObjectProvider<JavaMailSender> correo, Environment environment, Clock clock) {
        this.comprobantes = comprobantes; this.detalles = detalles; this.cliente = cliente; this.payload = payload;
        this.config = config; this.correo = correo; this.environment = environment; this.clock = clock;
    }

    // Envío automático después de que SUNAT acepta: solo si hay correo y todavía no se envió. Nunca lanza.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviarSiCorresponde(Long id) {
        try {
            var c = comprobantes.findById(id).orElse(null);
            if (c == null || c.getReceptorEmail() == null || c.getCorreoEnviadoEn() != null || !aceptado(c)) return;
            enviar(c, c.getReceptorEmail());
        } catch (RuntimeException ex) {
            // Queda sin correo_enviado_en: se puede reenviar con POST /comprobantes/{id}/correo.
            log.warn("No se pudo enviar el comprobante por correo | id={} | error={}", id, ex.getClass().getSimpleName());
        }
    }

    // Envío manual. destino null = el correo guardado en el comprobante.
    @Transactional
    public Comprobante enviarAhora(Comprobante c, String destino) {
        String email = destino == null || destino.isBlank() ? c.getReceptorEmail() : destino.trim().toLowerCase();
        if (email == null) throw new ReglaNegocioException("Indique el correo del cliente");
        if (!aceptado(c)) throw new ReglaNegocioException("Solo se envían comprobantes aceptados por SUNAT");
        c.setReceptorEmail(email);
        enviar(c, email);
        return c;
    }

    private void enviar(Comprobante c, String email) {
        JavaMailSender sender = correo.getIfAvailable();
        if (sender == null) throw new ServicioExternoException("El envío de correos no está configurado (spring.mail)");
        var ref = c.getReferenciaId() == null ? null : comprobantes.findById(c.getReferenciaId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Referencia no encontrada"));
        byte[] pdf = cliente.pdf(c.getTipo() == TipoComprobante.NOTA_CREDITO, payload.toPayload(c, detalles.findByComprobanteIdOrderByItem(c.getId()), ref));
        String numero = ComprobanteMapper.numero(c);
        try {
            var mensaje = sender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
            String remitente = environment.getProperty("spring.mail.username", "");
            if (!remitente.isBlank()) helper.setFrom(remitente, nombreRemitente());
            helper.setTo(email);
            helper.setSubject(asunto(c, numero));
            helper.setText(cuerpo(c, numero, ref));
            if (pdf != null && pdf.length > 0) helper.addAttachment(numero + ".pdf", new ByteArrayResource(pdf), "application/pdf");
            if (c.getXml() != null && !c.getXml().isBlank())
                helper.addAttachment(nombreXml(c), new ByteArrayResource(c.getXml().getBytes(StandardCharsets.UTF_8)), "application/xml");
            sender.send(mensaje);
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException ex) {
            log.warn("Fallo SMTP al enviar comprobante | id={} | error={}", c.getId(), ex.getClass().getSimpleName());
            throw new ServicioExternoException("No se pudo enviar el correo; intente de nuevo");
        }
        c.setCorreoEnviadoEn(LocalDateTime.now(clock));
        comprobantes.save(c);
        log.info("Comprobante enviado por correo | id={} | {}", c.getId(), numero);
    }

    private static boolean aceptado(Comprobante c) {
        return c.getEstado() == EstadoComprobante.ACEPTADO || c.getEstado() == EstadoComprobante.OBSERVADO;
    }

    private String nombreRemitente() {
        var e = config.getEmpresa();
        return e.getNombreComercial().isBlank() ? e.getRazonSocial() : e.getNombreComercial();
    }

    static String tipoTexto(TipoComprobante tipo) {
        return switch (tipo) {
            case BOLETA -> "Boleta de venta electrónica";
            case FACTURA -> "Factura electrónica";
            case NOTA_CREDITO -> "Nota de crédito electrónica";
        };
    }

    private String asunto(Comprobante c, String numero) {
        return (c.getAmbiente() == AmbienteFacturacion.BETA ? "[PRUEBA] " : "") + tipoTexto(c.getTipo()) + " " + numero + " - " + nombreRemitente();
    }

    private String cuerpo(Comprobante c, String numero, Comprobante ref) {
        var e = config.getEmpresa();
        var texto = new StringBuilder();
        texto.append("Estimado(a) ").append(c.getReceptorNombre()).append(":\n\n")
            .append("Le enviamos su ").append(tipoTexto(c.getTipo()).toLowerCase()).append(" ").append(numero)
            .append(" por un total de S/ ").append(c.getTotal().setScale(2)).append(".\n");
        if (ref != null) texto.append("Esta nota de crédito anula el comprobante ").append(ComprobanteMapper.numero(ref)).append(".\n");
        texto.append("\nAdjuntamos la representación impresa (PDF) y el archivo XML firmado.\n")
            .append("Puede verificar su validez en el portal de SUNAT con el RUC ").append(e.getRuc()).append(".\n");
        if (c.getAmbiente() == AmbienteFacturacion.BETA)
            texto.append("\nIMPORTANTE: este comprobante fue emitido en el ambiente de PRUEBA de SUNAT y no tiene valor tributario.\n");
        texto.append("\nGracias por su preferencia.\n").append(e.getRazonSocial()).append("\n").append(e.getDireccion()).append("\n");
        return texto.toString();
    }

    // Nombre SUNAT del XML: RUC-TIPO-SERIE-NUMERO.xml
    private String nombreXml(Comprobante c) {
        return config.getEmpresa().getRuc() + "-" + ComprobantePayloadMapper.tipoSunat(c.getTipo()) + "-" + c.getSerie() + "-" + c.getNumero() + ".xml";
    }
}
