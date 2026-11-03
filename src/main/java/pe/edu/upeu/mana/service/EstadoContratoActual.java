package pe.edu.upeu.mana.service;

import java.time.*;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.Contrato;
import pe.edu.upeu.mana.enums.EstadoContrato;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EstadoContratoActual {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EstadoContratoActual.class);

    private final Clock clock;

    public EstadoContratoActual(Clock clock) {
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public EstadoContrato calcular(Contrato c) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio EstadoContratoActual.calcular");
        try {
            if (c.getEstado() == EstadoContrato.ANULADO || c.getEstado() == EstadoContrato.FINALIZADO)
                return c.getEstado();
            var hoy = LocalDate.now(clock);
            if (c.getFechaFin().isBefore(hoy))
                return EstadoContrato.FINALIZADO;
            return c.getFechaInicio().isAfter(hoy) ? EstadoContrato.PROGRAMADO : EstadoContrato.ACTIVO;
        } finally {
            log.info("Fin EstadoContratoActual.calcular | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
