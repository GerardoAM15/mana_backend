package pe.edu.upeu.mana.service;

import java.math.*;
import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.DetalleOrden;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CalculadoraTotalesVenta {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CalculadoraTotalesVenta.class);

    @Transactional(readOnly = true)
    public BigDecimal recargo(List<DetalleOrden> lineas) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CalculadoraTotalesVenta.recargo");
        try {
            return lineas.stream().map(l -> l.getRecargoUnitario().multiply(BigDecimal.valueOf(l.getCantidad()))).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        } finally {
            log.info("Fin CalculadoraTotalesVenta.recargo | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal total(List<DetalleOrden> lineas) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CalculadoraTotalesVenta.total");
        try {
            var total = lineas.stream().map(l -> l.getPrecioUnitario().multiply(BigDecimal.valueOf(l.getCantidad()))).reduce(recargo(lineas), BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            if (total.signum() <= 0 || total.compareTo(new BigDecimal("999999999999.99")) > 0)
                throw new ReglaNegocioException("El total está fuera del importe permitido");
            return total;
        } finally {
            log.info("Fin CalculadoraTotalesVenta.total | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
