package pe.edu.upeu.mana.service;

import java.util.*;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.TipoLineaVenta;
import pe.edu.upeu.mana.enums.EstadoVenta;
import pe.edu.upeu.mana.enums.RolUsuario;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.comun.PagoRequestDTO;
import pe.edu.upeu.mana.mapper.VentaMapper;
import pe.edu.upeu.mana.service.service.ContextoLocalService;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.impl.VentaServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import pe.edu.upeu.mana.dto.venta.LineaVentaRequestDTO;
import pe.edu.upeu.mana.dto.venta.PagosVentaRequestDTO;
import pe.edu.upeu.mana.dto.venta.VentaRequestDTO;
import pe.edu.upeu.mana.service.service.CajaService;
import pe.edu.upeu.mana.service.service.StockService;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    CajaService cajas;

    @Mock
    ServicioRepository servicios;

    @Mock
    PlatoRepository platos;

    @Mock
    PrecioSueltoRepository precios;

    @Mock
    StockService stocks;

    @Mock
    VentaRepository ventas;

    @Mock ContextoLocalService contexto;
    @Mock PagoVentaRepository pagos;
    @Mock DevolucionVentaRepository devoluciones;
    @Mock DetalleOrdenRepository detalles;
    @Mock VentaMapper mapper;
    @Mock ValidadorPagos validadorPagos;
    @Mock Clock clock;

    @InjectMocks
    VentaServiceImpl service;

    @Test
    void cenaSinSueltosRespondeReglaDeNegocio() {
        var caja = new SesionCaja();
        caja.setLocalId(1L);
        when(cajas.buscarActualParaActualizar()).thenReturn(caja);
        var s = new Servicio();
        when(servicios.findById("C")).thenReturn(Optional.of(s));
        var p = new Plato();
        p.setId(2L);
        p.setCategoria("SEGUNDO");
        when(platos.findById(2L)).thenReturn(Optional.of(p));
        var ex = assertThrows(ReglaNegocioException.class, () -> service.crear(new VentaRequestDTO("C", false, null, List.of(new LineaVentaRequestDTO(TipoLineaVenta.PLATO, 1L, 2L, null, null, null)), null)));
        assertEquals("En la cena solo se vende el menú completo", ex.getMessage());
        verifyNoInteractions(ventas);
    }

    @Test
    void cajaCerradaImpideCrearLaVenta() {
        when(cajas.buscarActualParaActualizar()).thenThrow(new ReglaNegocioException("Debe tener una caja abierta"));
        assertThrows(ReglaNegocioException.class, () -> service.crear(new VentaRequestDTO("A", false, null, List.of(), null)));
        verifyNoInteractions(ventas, stocks);
    }

    @Test
    void cajaOriginalCerradaImpideCobrar() {
        when(ventas.buscarCajaId(1L)).thenReturn(Optional.of(2L));
        when(cajas.buscarAbiertaParaActualizar(2L)).thenThrow(new ReglaNegocioException("La caja está cerrada"));
        assertThrows(ReglaNegocioException.class, () -> service.cobrar(1L, new PagosVentaRequestDTO(List.of())));
        verify(ventas, never()).buscarParaActualizar(any());
    }

    @ParameterizedTest
    @EnumSource(value = RolUsuario.class, names = {"CAJERO", "DUENO"})
    void cobrarCajaAjenaRechazaAntesDeModificarLaVenta(RolUsuario rol) {
        cajaYUsuario(rol, 3L);

        assertThrows(AccessDeniedException.class, () -> service.cobrar(1L, requestPagos()));

        verify(ventas, never()).buscarParaActualizar(any());
        verifyNoInteractions(pagos, devoluciones, stocks, validadorPagos);
    }

    @ParameterizedTest
    @EnumSource(value = RolUsuario.class, names = {"CAJERO", "DUENO"})
    void anularCajaAjenaRechazaAntesDeDevolverDineroOStock(RolUsuario rol) {
        cajaYUsuario(rol, 3L);

        assertThrows(AccessDeniedException.class, () -> service.anular(1L, new AnularRequestDTO("Anulación de prueba")));

        verify(ventas, never()).buscarParaActualizar(any());
        verifyNoInteractions(pagos, devoluciones, stocks);
    }

    @ParameterizedTest
    @EnumSource(value = RolUsuario.class, names = {"CAJERO", "ADMINISTRADOR"})
    void propietarioOAdministradorPuedeCobrar(RolUsuario rol) {
        cajaYUsuario(rol, rol == RolUsuario.ADMINISTRADOR ? 3L : 2L);
        var venta = ventaPendiente();
        reloj();

        assertDoesNotThrow(() -> service.cobrar(1L, requestPagos()));

        assertEquals(EstadoVenta.PAGADA, venta.getEstado());
        verify(pagos).save(argThat(p -> p.getVentaId().equals(1L) && p.getMonto().equals(new BigDecimal("10.00"))));
    }

    @ParameterizedTest
    @EnumSource(value = RolUsuario.class, names = {"CAJERO", "ADMINISTRADOR"})
    void propietarioOAdministradorPuedeAnular(RolUsuario rol) {
        cajaYUsuario(rol, rol == RolUsuario.ADMINISTRADOR ? 3L : 2L);
        var venta = ventaPendiente();
        reloj();

        assertDoesNotThrow(() -> service.anular(1L, new AnularRequestDTO("Anulación de prueba")));

        assertEquals(EstadoVenta.ANULADA, venta.getEstado());
        assertNotNull(venta.getAnuladaEn());
    }

    private void cajaYUsuario(RolUsuario rol, Long usuarioId) {
        var caja = new SesionCaja();
        caja.setId(2L);
        caja.setUsuarioId(2L);
        caja.setLocalId(1L);
        when(ventas.buscarCajaId(1L)).thenReturn(Optional.of(2L));
        when(cajas.buscarAbiertaParaActualizar(2L)).thenReturn(caja);
        var usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setRol(rol);
        usuario.setCorreo("prueba@example.invalid");
        when(contexto.usuario()).thenReturn(usuario);
    }

    private Venta ventaPendiente() {
        var venta = new Venta();
        venta.setId(1L);
        venta.setCajaId(2L);
        venta.setLocalId(1L);
        venta.setEstado(EstadoVenta.PENDIENTE);
        venta.setTotal(new BigDecimal("10.00"));
        when(ventas.buscarParaActualizar(1L)).thenReturn(Optional.of(venta));
        return venta;
    }

    private void reloj() {
        when(clock.instant()).thenReturn(Instant.parse("2026-11-03T15:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("America/Lima"));
    }

    private PagosVentaRequestDTO requestPagos() {
        return new PagosVentaRequestDTO(List.of(new PagoRequestDTO("YAPE", new BigDecimal("10.00"), null)));
    }
}
