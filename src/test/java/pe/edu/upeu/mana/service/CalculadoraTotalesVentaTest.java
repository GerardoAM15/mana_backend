package pe.edu.upeu.mana.service;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.mana.entity.DetalleOrden;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraTotalesVentaTest {

    private final CalculadoraTotalesVenta calculadora = new CalculadoraTotalesVenta();

    @Test
    void dosMenusAlmuerzoParaLlevar() {
        assertEquals(new BigDecimal("26.00"), calculadora.total(List.of(linea(2, "11", "2"))));
    }

    @Test
    void almuerzoYEntradaParaLlevar() {
        assertEquals(new BigDecimal("18.00"), calculadora.total(List.of(linea(1, "11", "2"), linea(1, "4", "1"))));
    }

    @Test
    void desayunoParaLlevarSinRecargo() {
        assertEquals(new BigDecimal("4.50"), calculadora.total(List.of(linea(1, "4.50", "0"))));
    }

    private DetalleOrden linea(long cantidad, String precio, String recargo) {
        var d = new DetalleOrden();
        d.setCantidad(cantidad);
        d.setPrecioUnitario(new BigDecimal(precio));
        d.setRecargoUnitario(new BigDecimal(recargo));
        return d;
    }
}
