package pe.edu.upeu.mana.service;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.dto.comprobante.ComprobanteRequestDTO;
import pe.edu.upeu.mana.enums.TipoSolicitudComprobante;
import pe.edu.upeu.mana.exception.DatosInvalidosException;
import pe.edu.upeu.mana.service.service.ConsultaDocumentoService;

class ComprobanteFundamentosTest {
    private static BigDecimal n(String v) { return new BigDecimal(v); }
    @Test void cuadreExactoConDecimalesYRecargoIncluido() {
        var c = new CalculadoraComprobante();
        var lineas = List.of(
            new CalculadoraComprobante.Linea("1", "Menú para llevar", "NIU", n("3"), n("9.99").add(n("0.50"))),
            new CalculadoraComprobante.Linea("2", "Bebida", "NIU", n("2"), n("1.17")),
            new CalculadoraComprobante.Linea("3", "Adicional", "NIU", n("1"), n("0.01")));
        var resultado = c.calcular(lineas, n("33.82"), n("18.00"));
        assertEquals(n("33.82"), resultado.stream().map(d -> d.getTotal()).reduce(n("0.00"), BigDecimal::add));
        assertEquals(n("28.66"), resultado.stream().map(d -> d.getValorVenta()).reduce(n("0.00"), BigDecimal::add));
        assertEquals(n("5.16"), resultado.stream().map(d -> d.getIgv()).reduce(n("0.00"), BigDecimal::add));
        resultado.forEach(d -> assertEquals(d.getTotal(), d.getValorVenta().add(d.getIgv())));
        assertEquals(n("10.4900"), resultado.getFirst().getPrecioUnitario());
    }
    @Test void ultimaLineaAjustaElCentavoDeBase() {
        var l = new CalculadoraComprobante.Linea("1", "Producto", "NIU", n("1"), n("0.03"));
        var ds = new CalculadoraComprobante().calcular(List.of(l,l,l), n("0.09"), n("18"));
        assertEquals(n("0.08"), ds.stream().map(d -> d.getValorVenta()).reduce(n("0.00"), BigDecimal::add));
        assertEquals(n("0.02"), ds.getLast().getValorVenta());
    }
    @Test void facturaSinRucEsInvalida() {
        var v = new ValidadorReceptorComprobante(new FacturacionProperties(), mock(ConsultaDocumentoService.class));
        assertThrows(DatosInvalidosException.class, () -> v.validar(new ComprobanteRequestDTO(TipoSolicitudComprobante.FACTURA, null, null, null, null), n("10")));
    }
    @Test void boletaEnUmbralRequiereIdentificacion() {
        var v = new ValidadorReceptorComprobante(new FacturacionProperties(), mock(ConsultaDocumentoService.class));
        assertThrows(DatosInvalidosException.class, () -> v.validar(new ComprobanteRequestDTO(TipoSolicitudComprobante.BOLETA, "0", null, null, null), n("700.00")));
        assertEquals("CLIENTES VARIOS", v.validar(new ComprobanteRequestDTO(TipoSolicitudComprobante.BOLETA, null, null, null, null), n("699.99")).nombre());
    }
    @Test void dniYRucInvalidosSeRechazan() {
        var v = new ValidadorReceptorComprobante(new FacturacionProperties(), mock(ConsultaDocumentoService.class));
        assertThrows(DatosInvalidosException.class, () -> v.validar(new ComprobanteRequestDTO(TipoSolicitudComprobante.BOLETA, "1", "123", "Cliente", null), n("10")));
        assertThrows(DatosInvalidosException.class, () -> v.validar(new ComprobanteRequestDTO(TipoSolicitudComprobante.FACTURA, "6", "99123456789", "Empresa", null), n("10")));
    }
    @Test void cienLineasPequenasNoGeneranIgvNegativo() {
        var l=new CalculadoraComprobante.Linea("1","Producto","NIU",n("1"),n("0.03"));
        var ds=new CalculadoraComprobante().calcular(java.util.Collections.nCopies(100,l),n("3.00"),n("18"));
        assertEquals(n("2.54"),ds.stream().map(d -> d.getValorVenta()).reduce(n("0.00"),BigDecimal::add));
        assertTrue(ds.stream().allMatch(d -> d.getIgv().signum()>=0 && d.getValorVenta().signum()>=0));
    }
}
