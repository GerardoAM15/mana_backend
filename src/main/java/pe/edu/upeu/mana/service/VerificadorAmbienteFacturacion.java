package pe.edu.upeu.mana.service;

import java.time.*;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.exception.ServicioExternoException;

@Component
public class VerificadorAmbienteFacturacion {
    public enum Estado { COINCIDE, DISCREPANCIA, TEMPORAL }
    public record Resultado(Estado estado, String mensaje) {}
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VerificadorAmbienteFacturacion.class);
    private final ApisPeruFacturacionCliente cliente;
    private final LectorAmbienteEmpresa lector;
    private final FacturacionProperties config;
    private final Clock clock;
    private Resultado cache;
    private Instant vence = Instant.MIN;
    public VerificadorAmbienteFacturacion(ApisPeruFacturacionCliente cliente, LectorAmbienteEmpresa lector, FacturacionProperties config, Clock clock) {
        this.cliente=cliente; this.lector=lector; this.config=config; this.clock=clock;
    }
    @EventListener(ApplicationReadyEvent.class)
    public void iniciar() { if (config.isHabilitada()) verificar(); }
    public synchronized Resultado verificar() {
        if (cache != null && clock.instant().isBefore(vence)) return cache;
        try {
            var empresa = cliente.empresa();
            var environment = empresa == null ? null : empresa.get("environment");
            var valor = lector.leer(environment);
            if (valor.isEmpty()) {
                log.warn("No se pudo determinar el ambiente en APIsPERU | environment={}", environment);
                cache = new Resultado(Estado.TEMPORAL, "No se pudo determinar el ambiente de la empresa en APIsPERU; se reintentará");
            } else {
                boolean beta = valor.get().equals("beta") || valor.get().equals("nubefact_beta");
                boolean coincide = beta == (config.getAmbiente() == pe.edu.upeu.mana.enums.AmbienteFacturacion.BETA);
                cache = coincide ? new Resultado(Estado.COINCIDE, null) : new Resultado(Estado.DISCREPANCIA,
                    "El ambiente configurado en MANÁ (" + config.getAmbiente() + ") no coincide con el de la empresa en APIsPERU (" + valor.get() + ")");
                if (!coincide) log.warn("{}", cache.mensaje());
            }
        } catch (ServicioExternoException ex) {
            cache = new Resultado(Estado.TEMPORAL, "No se pudo consultar el ambiente de la empresa en APIsPERU; se reintentará");
            log.warn("No se pudo consultar el ambiente de facturación; se reintentará");
        }
        vence = clock.instant().plus(Duration.ofMinutes(10));
        return cache;
    }
}
