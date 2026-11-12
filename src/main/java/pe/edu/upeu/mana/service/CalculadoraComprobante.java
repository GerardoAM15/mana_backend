package pe.edu.upeu.mana.service;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.ComprobanteDetalle;
import pe.edu.upeu.mana.exception.DatosInvalidosException;
@Component
public class CalculadoraComprobante {
    public record Linea(String codigo, String descripcion, String unidad, BigDecimal cantidad, BigDecimal precio) {}
    public List<ComprobanteDetalle> calcular(List<Linea> lineas, BigDecimal total, BigDecimal porcentaje) {
        if (lineas.isEmpty() || total.signum() <= 0 || porcentaje.signum() < 0 || porcentaje.compareTo(new BigDecimal("100")) > 0)
            throw new DatosInvalidosException("Montos de comprobante inválidos");
        BigDecimal factor = BigDecimal.ONE.add(porcentaje.movePointLeft(2));
        BigDecimal baseTotal = total.divide(factor, 2, RoundingMode.HALF_UP);
        BigDecimal suma = BigDecimal.ZERO, bases = BigDecimal.ZERO;
        var resultado = new ArrayList<ComprobanteDetalle>();
        for (int i = 0; i < lineas.size(); i++) {
            var l = lineas.get(i);
            if (l.cantidad().signum() <= 0 || l.precio().signum() < 0) throw new DatosInvalidosException("Línea de comprobante inválida");
            boolean ultima = i == lineas.size() - 1;
            BigDecimal importe = ultima ? total.subtract(suma) : l.precio().multiply(l.cantidad()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal base = ultima ? baseTotal.subtract(bases) : importe.divide(factor, 2, RoundingMode.HALF_UP);
            BigDecimal baseRestante = baseTotal.subtract(bases);
            BigDecimal totalPosterior = total.subtract(suma).subtract(importe);
            base = base.max(baseRestante.subtract(totalPosterior).max(BigDecimal.ZERO)).min(baseRestante).min(importe);
            if (base.signum() < 0 || importe.compareTo(base) < 0) throw new DatosInvalidosException("Las líneas no cuadran con el total");
            var d = new ComprobanteDetalle();
            d.setItem(i + 1); d.setCodigo(l.codigo()); d.setDescripcion(l.descripcion()); d.setUnidad(l.unidad());
            d.setCantidad(l.cantidad()); d.setPrecioUnitario(l.precio().setScale(4, RoundingMode.HALF_UP));
            d.setValorUnitario(l.precio().divide(factor, 4, RoundingMode.HALF_UP));
            d.setValorVenta(base); d.setIgv(importe.subtract(base)); d.setTotal(importe);
            resultado.add(d); suma = suma.add(importe); bases = bases.add(base);
        }
        return resultado;
    }
}
