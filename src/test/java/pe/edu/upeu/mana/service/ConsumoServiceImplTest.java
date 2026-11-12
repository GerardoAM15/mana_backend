package pe.edu.upeu.mana.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.EstadoContrato;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.mapper.ConsumoMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.impl.ConsumoServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.consumo.ConsumoRequestDTO;
import pe.edu.upeu.mana.dto.consumo.PlatoCantidadRequestDTO;
import pe.edu.upeu.mana.service.service.CajaService;
import pe.edu.upeu.mana.service.service.ContextoLocalService;

@ExtendWith(MockitoExtension.class)
class ConsumoServiceImplTest {

    @Mock
    ContratoRepository contratos;

    @Mock
    DetalleContratoRepository detallesContrato;

    @Mock
    ConsumoRepository consumos;

    @Mock
    DetalleConsumoRepository detallesConsumo;

    @Mock
    PlatoRepository platos;

    @Mock
    ServicioDiaAtencionRepository dias;

    @Mock
    DiaNoAtencionRepository cierres;

    @Mock
    ServicioRepository servicios;

    @Mock
    MetodoPagoRepository metodos;

    @Mock
    ContextoLocalService contexto;

    @Mock
    CajaService cajas;

    @Mock
    ValidadorMenu menus;

    @Mock
    ConsumoMapper mapper;

    Clock clock = Clock.fixed(Instant.parse("2026-11-02T15:00:00Z"), ZoneId.of("America/Lima"));

    ConsumoServiceImpl service;

    Contrato contrato;

    DetalleContrato detalle;

    @BeforeEach
    void preparar() {
        service = crearServicio(clock);
        contrato = new Contrato();
        contrato.setId(1L);
        contrato.setClienteId(2L);
        contrato.setLocalId(1L);
        contrato.setEstado(EstadoContrato.ACTIVO);
        contrato.setFechaInicio(LocalDate.parse("2026-11-02"));
        contrato.setFechaFin(LocalDate.parse("2026-11-08"));
        detalle = new DetalleContrato();
        detalle.setId(3L);
        detalle.setContratoId(1L);
        detalle.setServicio("A");
        detalle.setSaldoRaciones(1L);
        detalle.setRacionesContratadas(5L);
        detalle.setPrecioRacionPactado(new BigDecimal("10.00"));
    }

    private ConsumoServiceImpl crearServicio(Clock reloj) {
        return new ConsumoServiceImpl(contratos, detallesContrato, consumos, detallesConsumo, platos, new ValidadorAtencion(servicios, dias, cierres), servicios, metodos, contexto, cajas, menus, new EstadoContratoActual(reloj), mapper, reloj);
    }

    private void base(String servicio, boolean atiende) {
        when(contexto.localId(null)).thenReturn(1L);
        var regla = new ReglaCobro();
        regla.setId(1L);
        regla.setServicio(servicio);
        regla.setRecargoLlevar(new BigDecimal("2.00"));
        when(menus.obtenerRegla(servicio)).thenReturn(regla);
        when(contratos.buscarParaActualizar(1L)).thenReturn(Optional.of(contrato));
        var s = new Servicio();
        s.setCodigo(servicio);
        s.setNombre(servicio.equals("C") ? "Cena" : "Almuerzo");
        when(servicios.findById(servicio)).thenReturn(Optional.of(s));
        when(dias.existsById(new ServicioDiaAtencionId(servicio, LocalDate.now(clock).getDayOfWeek().getValue()))).thenReturn(atiende);
    }

    @Test
    void saldoCeroRechazaElConsumo() {
        base("A", true);
        detalle.setSaldoRaciones(0L);
        when(detallesContrato.buscarServicioParaActualizar(1L, "A")).thenReturn(Optional.of(detalle));
        assertThrows(ReglaNegocioException.class, () -> service.crear(request("A")));
        verify(consumos, never()).saveAndFlush(any());
    }

    @Test
    void cenaEnViernesDevuelveUnMensajeClaro() {
        clock = Clock.fixed(Instant.parse("2026-11-06T15:00:00Z"), ZoneId.of("America/Lima"));
        service = crearServicio(clock);
        base("C", false);
        var ex = assertThrows(ReglaNegocioException.class, () -> service.crear(request("C")));
        assertTrue(ex.getMessage().contains("viernes"));
        verifyNoInteractions(detallesContrato);
    }

    @Test
    void consumoDobleNoDescuentaOtraRacion() {
        base("A", true);
        when(detallesContrato.buscarServicioParaActualizar(1L, "A")).thenReturn(Optional.of(detalle));
        when(consumos.existsByClaveDiaria("2:2026-11-02:A")).thenReturn(true);
        var ex = assertThrows(ReglaNegocioException.class, () -> service.crear(request("A")));
        assertEquals("Ya consumió el almuerzo hoy", ex.getMessage());
        assertEquals(1L, detalle.getSaldoRaciones());
        verify(consumos, never()).saveAndFlush(any());
    }

    @Test
    void feriadoLocalImpideConsumir() {
        when(contexto.localId(null)).thenReturn(1L);
        var regla = new ReglaCobro();
        regla.setRecargoLlevar(BigDecimal.ZERO);
        when(menus.obtenerRegla("A")).thenReturn(regla);
        when(contratos.buscarParaActualizar(1L)).thenReturn(Optional.of(contrato));
        var s = new Servicio();
        when(servicios.findById("A")).thenReturn(Optional.of(s));
        when(cierres.existsByLocalIdAndFecha(1L, LocalDate.parse("2026-11-02"))).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> service.crear(request("A")));
        verifyNoInteractions(detallesContrato);
    }

    @Test
    void recargoConCajaCerradaNoConsumeSaldo() {
        when(contexto.localId(null)).thenReturn(1L);
        var regla = new ReglaCobro();
        regla.setRecargoLlevar(new BigDecimal("2.00"));
        when(menus.obtenerRegla("A")).thenReturn(regla);
        var m = new MetodoPago();
        when(metodos.findById("YAPE")).thenReturn(Optional.of(m));
        when(cajas.buscarActualParaActualizar()).thenThrow(new ReglaNegocioException("Debe tener una caja abierta"));
        assertThrows(ReglaNegocioException.class, () -> service.crear(new ConsumoRequestDTO(1L, "A", request("A").getPlatos(), true, "YAPE")));
        assertEquals(1L, detalle.getSaldoRaciones());
        verifyNoInteractions(contratos);
    }

    @Test
    void anularDevuelveUnaRacionYLiberaLaClave() {
        var ubicacion = mock(ConsumoRepository.UbicacionConsumo.class);
        when(ubicacion.getContratoId()).thenReturn(1L);
        when(ubicacion.getDetalleId()).thenReturn(3L);
        when(consumos.buscarUbicacion(8L)).thenReturn(Optional.of(ubicacion));
        when(contratos.buscarParaActualizar(1L)).thenReturn(Optional.of(contrato));
        when(detallesContrato.buscarParaActualizar(3L)).thenReturn(Optional.of(detalle));
        var c = new Consumo();
        c.setId(8L);
        c.setRecargoLlevar(BigDecimal.ZERO);
        c.setFecha(LocalDate.parse("2026-11-02"));
        c.setClaveDiaria("2:2026-11-02:A");
        when(consumos.buscarParaActualizar(8L)).thenReturn(Optional.of(c));
        var u = new Usuario();
        u.setCorreo("prueba@example.invalid");
        when(contexto.usuario()).thenReturn(u);
        service.anular(8L, new AnularRequestDTO("Corrección de consumo"));
        assertEquals(2L, detalle.getSaldoRaciones());
        assertTrue(c.getAnulado());
        assertNull(c.getClaveDiaria());
        assertNotNull(c.getAnuladoEn());
    }

    @Test void anularRecargoSinEfectivoNoModificaElConsumoNiElSaldo() {
        var ubicacion = mock(ConsumoRepository.UbicacionConsumo.class);
        when(ubicacion.getContratoId()).thenReturn(1L);
        when(ubicacion.getDetalleId()).thenReturn(3L);
        when(ubicacion.getCajaId()).thenReturn(5L);
        when(consumos.buscarUbicacion(8L)).thenReturn(Optional.of(ubicacion));
        when(contratos.buscarParaActualizar(1L)).thenReturn(Optional.of(contrato));
        when(detallesContrato.buscarParaActualizar(3L)).thenReturn(Optional.of(detalle));
        var c = new Consumo(); c.setFecha(LocalDate.now(clock)); c.setCajaId(5L);
        c.setRecargoLlevar(new BigDecimal("2.00")); c.setMetodoRecargo("EFECTIVO"); c.setAnulado(false);
        when(consumos.buscarParaActualizar(8L)).thenReturn(Optional.of(c));
        doThrow(new ReglaNegocioException("No hay efectivo suficiente en caja para la devolución"))
                .when(cajas).validarDevolucion(5L, Map.of("EFECTIVO",new BigDecimal("2.00")));
        assertEquals("No hay efectivo suficiente en caja para la devolución",assertThrows(ReglaNegocioException.class,
                () -> service.anular(8L,new AnularRequestDTO("Corrección de consumo"))).getMessage());
        assertFalse(c.getAnulado()); assertEquals(1L,detalle.getSaldoRaciones()); verify(consumos,never()).flush();
    }

    private ConsumoRequestDTO request(String s) {
        return new ConsumoRequestDTO(1L, s, List.of(new PlatoCantidadRequestDTO(1L, 1L)), false, null);
    }
}
