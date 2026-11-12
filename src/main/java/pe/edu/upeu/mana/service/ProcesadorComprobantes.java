package pe.edu.upeu.mana.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.domain.PageRequest;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.enums.EstadoComprobante;
import pe.edu.upeu.mana.repository.ComprobanteRepository;
import java.util.List;
import pe.edu.upeu.mana.service.service.EnvioComprobanteService;

@Component
public class ProcesadorComprobantes {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(ProcesadorComprobantes.class);
    private final EnvioComprobanteService envio;
    private final ComprobanteRepository comprobantes;
    private final FacturacionProperties config;
    private final CorreoComprobante correo;
    public ProcesadorComprobantes(EnvioComprobanteService envio, ComprobanteRepository comprobantes, FacturacionProperties config, CorreoComprobante correo) {
        this.envio=envio; this.comprobantes=comprobantes; this.config=config; this.correo=correo;
    }
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void despuesDelCommit(ComprobanteReservado evento) { procesar(evento.id()); }
    private void procesar(Long id) {
        try {
            envio.enviar(id);
            // Si SUNAT lo aceptó y el cliente dejó su correo, se le envía el PDF y el XML (nunca lanza).
            correo.enviarSiCorresponde(id);
        }
        catch (RuntimeException ex) {
            // El commit del cobro ya terminó: jamás propagar un fallo de facturación al cliente del cobro.
            log.warn("No se pudo procesar comprobante; queda disponible para reintento | id={} | error={}",id,ex.getClass().getSimpleName());
        }
    }
    @Scheduled(cron="0 */5 * * * *")
    public void reintentar() {
        if(!config.isHabilitada()) return;
        long inicio=System.nanoTime(); log.debug("Inicio ProcesadorComprobantes.reintentar");
        try {
            // Cursor estable: los envíos cambian el conjunto pendiente; paginar por offset omitiría filas.
            long ultimo=0;
            while(true) {
                var ids=comprobantes.reintentablesDespues(List.of(EstadoComprobante.PENDIENTE,EstadoComprobante.ERROR),ultimo,PageRequest.of(0,100));
                if(ids.isEmpty()) break;
                for(var id:ids) { procesar(id); ultimo=id; }
            }
        } finally { log.debug("Fin ProcesadorComprobantes.reintentar | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
}
