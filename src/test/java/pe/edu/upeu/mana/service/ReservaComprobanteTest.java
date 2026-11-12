package pe.edu.upeu.mana.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.dto.comprobante.ComprobanteRequestDTO;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.impl.ReservaComprobanteServiceImpl;
import pe.edu.upeu.mana.service.service.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaComprobanteTest {
    ComprobanteRepository comprobantes=mock(ComprobanteRepository.class);
    ComprobanteDetalleRepository detalles=mock(ComprobanteDetalleRepository.class);
    VentaRepository ventas=mock(VentaRepository.class);
    ContratoRepository contratos=mock(ContratoRepository.class);
    DetalleOrdenRepository ordenes=mock(DetalleOrdenRepository.class);
    DetalleContratoRepository serviciosContrato=mock(DetalleContratoRepository.class);
    ServicioRepository servicios=mock(ServicioRepository.class);
    CorrelativoComprobante correlativos=mock(CorrelativoComprobante.class);
    ContextoLocalService contexto=mock(ContextoLocalService.class);
    ApplicationEventPublisher eventos=mock(ApplicationEventPublisher.class);
    FacturacionProperties config=new FacturacionProperties();
    Clock clock=Clock.fixed(Instant.parse("2026-11-10T15:00:00Z"),ZoneId.of("America/Lima"));
    ReservaComprobanteServiceImpl reserva;
    ComprobanteRequestDTO boleta=new ComprobanteRequestDTO(TipoSolicitudComprobante.BOLETA,null,null,null,null);
    @BeforeEach void preparar() {
        config.setHabilitada(true); var e=config.getEmpresa(); e.setRuc("20123456789"); e.setRazonSocial("MANÁ"); e.setDireccion("Dirección"); e.setUbigeo("150101");
        reserva=new ReservaComprobanteServiceImpl(comprobantes,detalles,ventas,contratos,ordenes,serviciosContrato,servicios,correlativos,
            new CalculadoraComprobante(),new ValidadorReceptorComprobante(config,mock(ConsultaDocumentoService.class)),contexto,config,eventos,clock);
    }
    Venta pagada() {
        var u=new Usuario(); u.setRol(RolUsuario.CAJERO); when(contexto.usuario()).thenReturn(u);
        var v=new Venta(); v.setId(1L); v.setLocalId(2L); v.setTotal(new BigDecimal("118.00")); v.setEstado(EstadoVenta.PAGADA);
        when(ventas.buscarParaActualizar(1L)).thenReturn(Optional.of(v)); return v;
    }
    @ParameterizedTest @EnumSource(value=EstadoComprobante.class,names={"PENDIENTE","ERROR","ACEPTADO","OBSERVADO"})
    void segundaBoletaVigenteEsConflicto(EstadoComprobante estado) {
        pagada(); var actual=new Comprobante(); actual.setId(5L); actual.setEstado(estado);
        when(comprobantes.findByVentaIdAndEstadoNot(1L,EstadoComprobante.ANULADO)).thenReturn(Optional.of(actual));
        when(comprobantes.bloquear(5L)).thenReturn(Optional.of(actual));
        assertThrows(ReglaNegocioException.class,() -> reserva.venta(1L,boleta));
        assertEquals(estado,actual.getEstado()); verifyNoInteractions(correlativos,eventos);
    }
    @Test void rechazadoSeAnulaYPermiteEmitirOtro() {
        pagada(); var rechazado=new Comprobante(); rechazado.setId(5L); rechazado.setEstado(EstadoComprobante.RECHAZADO);
        when(comprobantes.findByVentaIdAndEstadoNot(1L,EstadoComprobante.ANULADO)).thenReturn(Optional.of(rechazado));
        when(comprobantes.bloquear(5L)).thenReturn(Optional.of(rechazado));
        var d=new DetalleOrden(); d.setId(3L); d.setDescripcion("Almuerzo"); d.setCantidad(2L); d.setPrecioUnitario(new BigDecimal("59.00")); d.setRecargoUnitario(BigDecimal.ZERO);
        when(ordenes.findByVentaIdOrderById(1L)).thenReturn(List.of(d));
        when(correlativos.siguiente(2L,TipoSerie.BOLETA)).thenReturn(new CorrelativoComprobante.Numero(7L,"B001",2));
        when(comprobantes.saveAndFlush(any())).thenAnswer(a -> { Comprobante c=a.getArgument(0); if(c.getId()==null) c.setId(99L); return c; });
        assertEquals(99L,reserva.venta(1L,boleta));
        assertEquals(EstadoComprobante.ANULADO,rechazado.getEstado());
        // El rechazado se escribe como ANULADO antes de insertar el nuevo (índice único de comprobante vigente).
        var orden=inOrder(comprobantes);
        orden.verify(comprobantes).saveAndFlush(rechazado);
        orden.verify(comprobantes).saveAndFlush(argThat(c -> c!=rechazado && c.getNumero()==2 && c.getEstado()==EstadoComprobante.PENDIENTE));
        verify(eventos).publishEvent(new ComprobanteReservado(99L));
    }
    @Test void reservaCopiaLineasConRecargoYPublicaEvento() {
        var v=pagada(); var d=new DetalleOrden(); d.setId(3L); d.setDescripcion("Almuerzo"); d.setCantidad(2L); d.setPrecioUnitario(new BigDecimal("58.50")); d.setRecargoUnitario(new BigDecimal("0.50"));
        when(ordenes.findByVentaIdOrderById(1L)).thenReturn(List.of(d));
        when(correlativos.siguiente(2L,TipoSerie.BOLETA)).thenReturn(new CorrelativoComprobante.Numero(7L,"B001",1));
        when(comprobantes.saveAndFlush(any())).thenAnswer(a -> { Comprobante c=a.getArgument(0); c.setId(99L); return c; });
        assertEquals(99L,reserva.venta(1L,boleta));
        verify(detalles).save(argThat(l -> l.getPrecioUnitario().compareTo(new BigDecimal("59"))==0 && l.getTotal().compareTo(new BigDecimal("118"))==0));
        verify(eventos).publishEvent(new ComprobanteReservado(99L)); assertEquals(EstadoVenta.PAGADA,v.getEstado());
        d.setDescripcion("Editado después");
        verify(detalles).save(argThat(l -> l.getDescripcion().equals("Almuerzo")));
    }
    @Test void deshabilitadaYTicketNoCambianFlujo() {
        config.setHabilitada(false); assertNull(reserva.venta(1L,boleta));
        config.setHabilitada(true); assertNull(reserva.venta(1L,null));
        assertNull(reserva.venta(1L,new ComprobanteRequestDTO(TipoSolicitudComprobante.TICKET,null,null,null,null)));
        verifyNoInteractions(ventas,contratos,comprobantes,contexto,correlativos,eventos);
    }
    @ParameterizedTest @EnumSource(value=EstadoComprobante.class,names={"ACEPTADO","OBSERVADO","PENDIENTE","ERROR","RECHAZADO"})
    void anulacionSoloGeneraNotaCuandoFueAceptado(EstadoComprobante estado) {
        var original=new Comprobante(); original.setId(1L); original.setLocalId(2L); original.setTipo(TipoComprobante.BOLETA); original.setEstado(estado); original.setAmbiente(AmbienteFacturacion.BETA);
        original.setTotal(new BigDecimal("118.00")); original.setOpGravadas(new BigDecimal("100.00")); original.setIgv(new BigDecimal("18.00")); original.setPorcentajeIgv(new BigDecimal("18.00"));
        original.setReceptorTipoDoc("0"); original.setReceptorNombre("CLIENTES VARIOS");
        when(comprobantes.findByVentaIdAndEstadoNot(7L,EstadoComprobante.ANULADO)).thenReturn(Optional.of(original)); when(comprobantes.bloquear(1L)).thenReturn(Optional.of(original));
        boolean nota=estado==EstadoComprobante.ACEPTADO || estado==EstadoComprobante.OBSERVADO;
        if(nota) {
            when(correlativos.siguiente(2L,TipoSerie.NOTA_CREDITO_BOLETA)).thenReturn(new CorrelativoComprobante.Numero(8L,"BC01",1));
            when(comprobantes.saveAndFlush(any())).thenAnswer(a -> { Comprobante c=a.getArgument(0); c.setId(99L); return c; });
        }
        reserva.anularVenta(7L); assertEquals(EstadoComprobante.ANULADO,original.getEstado());
        if(nota) verify(comprobantes).saveAndFlush(argThat(c -> c.getTipo()==TipoComprobante.NOTA_CREDITO && c.getReferenciaId().equals(1L) && c.getVentaId()==null && c.getTotal().equals(original.getTotal()) && c.getMotivoCodigo().equals("01")));
        else { verify(comprobantes,never()).saveAndFlush(any()); verifyNoInteractions(correlativos,eventos); }
    }
    @Test void formatoNumero() { assertEquals("B001-00000001",new CorrelativoComprobante.Numero(1L,"B001",1).formateado()); }
}
