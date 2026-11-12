package pe.edu.upeu.mana.service.impl;

import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.service.service.EnvioComprobanteService;

import java.time.*;
import java.util.List;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.entity.Comprobante;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.ComprobantePayloadMapper;
import pe.edu.upeu.mana.repository.*;

@Service
public class EnvioComprobanteServiceImpl implements EnvioComprobanteService {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(EnvioComprobanteServiceImpl.class);
    // Debe coincidir con el límite de ComprobanteRepository.reintentables* (c.intentos < 5).
    private static final int MAX_INTENTOS=5;
    // SUNAT: "El comprobante fue registrado previamente".
    private static final int CODIGO_YA_REGISTRADO=1033;
    private final ComprobanteRepository comprobantes;
    private final ComprobanteDetalleRepository detalles;
    private final ApisPeruFacturacionCliente cliente;
    private final VerificadorAmbienteFacturacion ambientes;
    private final ComprobantePayloadMapper mapper;
    private final FacturacionProperties config;
    private final Clock clock;
    public EnvioComprobanteServiceImpl(ComprobanteRepository comprobantes, ComprobanteDetalleRepository detalles, ApisPeruFacturacionCliente cliente,
            VerificadorAmbienteFacturacion ambientes, ComprobantePayloadMapper mapper, FacturacionProperties config, Clock clock) {
        this.comprobantes=comprobantes; this.detalles=detalles; this.cliente=cliente; this.ambientes=ambientes; this.mapper=mapper; this.config=config; this.clock=clock;
    }
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void enviar(Long id) {
        long inicio=System.nanoTime(); log.debug("Inicio EnvioComprobanteService.enviar | id={}",id);
        try {
            if (!config.isHabilitada()) return;
            // Serializa envío, reintentos y anulación, incluso en varias instancias de la app.
            var c=comprobantes.bloquear(id).orElseThrow(() -> new RecursoNoEncontradoException("Comprobante no encontrado"));
            if (!List.of(EstadoComprobante.PENDIENTE,EstadoComprobante.ERROR).contains(c.getEstado()) || c.getIntentos()>=MAX_INTENTOS) return;
            var verificacion=ambientes.verificar();
            if (verificacion.estado()!=VerificadorAmbienteFacturacion.Estado.COINCIDE) {
                c.setEstado(EstadoComprobante.PENDIENTE); c.setSunatMensaje(recortar(verificacion.mensaje(),500)); return;
            }
            if(c.getAmbiente()!=config.getAmbiente()) {
                c.setEstado(EstadoComprobante.PENDIENTE); c.setSunatMensaje("El ambiente del comprobante ("+c.getAmbiente()+") no coincide con el configurado en MANÁ ("+config.getAmbiente()+")"); return;
            }
            c.setIntentos(c.getIntentos()+1); c.setUltimoIntentoEn(LocalDateTime.now(clock));
            try {
                var ref=c.getReferenciaId()==null?null:comprobantes.findById(c.getReferenciaId()).orElseThrow(() -> new RecursoNoEncontradoException("Referencia no encontrada"));
                var respuesta=cliente.enviar(c.getTipo()==TipoComprobante.NOTA_CREDITO,mapper.toPayload(c,detalles.findByComprobanteIdOrderByItem(id),ref));
                aplicar(c,respuesta);
            } catch(ServicioExternoException ex) {
                c.setEstado(EstadoComprobante.ERROR); c.setSunatMensaje(ex.getMessage());
            }
            comprobantes.flush();
        } finally { log.debug("Fin EnvioComprobanteService.enviar | id={} | duracionMs={}",id,(System.nanoTime()-inicio)/1_000_000); }
    }
    private void aplicar(Comprobante c, JsonNode r) {
        if(r==null || !r.isObject()) throw new ServicioExternoException("Respuesta de facturación ilegible; se reintentará");
        if(texto(r,"xml")!=null) c.setXml(texto(r,"xml"));
        if(texto(r,"hash")!=null) c.setHash(recortar(texto(r,"hash"),100));
        var sunat=r.path("sunatResponse");
        if(aplicarCdr(c,sunat)) return;
        var error=sunat.path("error");
        String codigo=texto(error,"code");
        c.setSunatCodigo(recortar(codigo,10));
        c.setSunatMensaje(recortar(error.path("message").asText("No se recibió CDR de SUNAT; se reintentará"),500));
        int numero=codigoNumerico(codigo);
        if(numero==CODIGO_YA_REGISTRADO) { resolverYaRegistrado(c); return; }
        // Códigos SUNAT 2000-3999: rechazo definitivo; reenviar los mismos datos no sirve.
        // El resto (0100-1999 y errores sin código) son excepciones o fallos de transporte: se reintentan.
        c.setEstado(numero>=2000 && numero<=3999 ? EstadoComprobante.RECHAZADO : EstadoComprobante.ERROR);
    }

    // Aplica el CDR si la respuesta lo trae; devuelve false si no hay CDR.
    private boolean aplicarCdr(Comprobante c, JsonNode sunat) {
        var cdr=sunat.path("cdrResponse");
        if(!cdr.isObject() || cdr.path("code").asText("").isBlank()) return false;
        if(texto(sunat,"cdrZip")!=null) c.setCdr(texto(sunat,"cdrZip"));
        c.setSunatCodigo(recortar(cdr.path("code").asText(),10));
        var mensaje=new StringBuilder(cdr.path("description").asText(""));
        var notas=cdr.path("notes"); if(notas.isArray()) notas.forEach(n -> mensaje.append("; ").append(n.asText()));
        c.setSunatMensaje(recortar(mensaje.toString(),500));
        // APIsPERU no envía "accepted" (verificado el 2026-11-10: solo id, code, description, notes): se decide
        // por el código SUNAT del CDR. 0 = aceptado; 4000+ = aceptado con observaciones; 2000-3999 = rechazado.
        var flag=cdr.get("accepted");
        int codigo=codigoNumerico(cdr.path("code").asText());
        boolean aceptado=flag!=null && flag.isBoolean() ? flag.asBoolean() : (codigo==0 || codigo>=4000);
        c.setEstado(aceptado ? (notas.isArray() && !notas.isEmpty()?EstadoComprobante.OBSERVADO:EstadoComprobante.ACEPTADO):EstadoComprobante.RECHAZADO);
        return true;
    }

    // 1033: SUNAT ya registró ese comprobante (p. ej. un envío anterior llegó pero se perdió la respuesta).
    // Reenviarlo no sirve: se consulta su CDR con GET /invoice/status. Si no se puede saber, se deja de
    // reintentar y se pide verificarlo a mano, para no repetir el envío indefinidamente.
    private void resolverYaRegistrado(Comprobante c) {
        if(c.getTipo()!=TipoComprobante.NOTA_CREDITO) {
            var estado=cliente.estado(c.getTipo()==TipoComprobante.FACTURA?"01":"03",c.getSerie(),c.getNumero());
            if(estado!=null && aplicarCdr(c,estado)) return;
        }
        c.setEstado(EstadoComprobante.ERROR);
        c.setIntentos(MAX_INTENTOS);
        c.setSunatMensaje("SUNAT indica que el comprobante ya fue registrado (1033) y no se pudo obtener su CDR; "
            + "verifique su estado en SUNAT o en el panel de APIsPERU antes de volver a emitirlo");
    }

    private static int codigoNumerico(String codigo) {
        try { return codigo==null ? -1 : Integer.parseInt(codigo.trim()); } catch(NumberFormatException e) { return -1; }
    }
    private String texto(JsonNode n,String campo) { var v=n.get(campo); return v==null || v.isNull()?null:v.asText(); }
    private String recortar(String s,int n) { return s==null?null:s.substring(0,Math.min(n,s.length())); }
}
