package pe.edu.upeu.mana.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.mapper.CajaMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.impl.CajaServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import pe.edu.upeu.mana.dto.caja.CerrarCajaRequestDTO;
import pe.edu.upeu.mana.dto.caja.ConteoCajaRequestDTO;
import pe.edu.upeu.mana.enums.EstadoVenta;
import pe.edu.upeu.mana.enums.RolUsuario;
import pe.edu.upeu.mana.service.service.ContextoLocalService;

@ExtendWith(MockitoExtension.class)
class CajaServiceImplTest {

    @Mock
    SesionCajaRepository cajas;

    @Mock
    UsuarioRepository usuarios;

    @Mock
    VentaRepository ventas;

    @Mock
    DetalleCierreCajaRepository detalles;

    @Mock
    MetodoPagoRepository metodos;

    @Mock
    ContextoLocalService contexto;

    CajaServiceImpl service;

    SesionCaja caja;

    Clock clock = Clock.fixed(Instant.parse("2026-11-03T15:00:00Z"), ZoneId.of("America/Lima"));

    @BeforeEach
    void preparar() {
        service = new CajaServiceImpl(cajas, usuarios, ventas, detalles, metodos, contexto, new CajaMapper(), clock);
        caja = new SesionCaja();
        caja.setId(10L);
        caja.setUsuarioId(2L);
        caja.setLocalId(1L);
        caja.setApertura(LocalDateTime.now(clock).minusHours(1));
        caja.setMontoInicial(new BigDecimal("0.00"));
        caja.setClaveAbierta("2");
        when(cajas.buscarParaActualizar(10L)).thenReturn(Optional.of(caja));
    }

    @Test
    void devolucionYapeSinCobrosPermiteCerrarConEsperadoMenosCien() {
        when(metodos.existsById("YAPE")).thenReturn(true);
        service.validarDevolucion(10L, Map.of("YAPE", new BigDecimal("100.00")));
        // El flujo de anulación ya guardó la devolución: la consulta devuelve su contribución neta.
        when(cajas.sumarMovimientos(10L)).thenReturn(List.of(movimiento("YAPE", "-100.00", 1L)));
        usuario(RolUsuario.CAJERO, 2L);
        var respuesta = service.cerrar(10L, new CerrarCajaRequestDTO(List.of(new ConteoCajaRequestDTO("YAPE", BigDecimal.ZERO)), null));
        assertEquals(new BigDecimal("-100.00"), respuesta.getMontoEsperado());
        assertEquals(new BigDecimal("0.00"), respuesta.getMontoContado());
        assertEquals(new BigDecimal("100.00"), respuesta.getDiferencia());
        assertNotNull(respuesta.getCierre());
        assertNull(caja.getClaveAbierta());
        var captor = ArgumentCaptor.forClass(DetalleCierreCaja.class);
        verify(detalles).save(captor.capture());
        assertEquals("YAPE", captor.getValue().getMetodo());
        assertEquals(new BigDecimal("-100.00"), captor.getValue().getMontoEsperado());
        assertEquals(new BigDecimal("100.00"), captor.getValue().getDiferencia());
    }

    @Test
    void efectivoMayorAlDisponibleRechazaLaDevolucion() {
        caja.setMontoInicial(new BigDecimal("50.00"));
        when(metodos.existsById("EFECTIVO")).thenReturn(true);
        when(cajas.sumarMovimientos(10L)).thenReturn(List.of(movimiento("EFECTIVO", "20.00", 1L)));
        var ex = assertThrows(ReglaNegocioException.class, () -> service.validarDevolucion(10L, Map.of("EFECTIVO", new BigDecimal("100.00"))));
        assertEquals("No hay efectivo suficiente en caja para la devolución", ex.getMessage());
        verifyNoInteractions(detalles);
    }

    @Test
    void efectivoExactamenteDisponibleIncluyeElMontoInicial() {
        caja.setMontoInicial(new BigDecimal("50.00"));
        when(metodos.existsById("EFECTIVO")).thenReturn(true);
        when(cajas.sumarMovimientos(10L)).thenReturn(List.of(movimiento("EFECTIVO", "20.00", 1L)));
        assertDoesNotThrow(() -> service.validarDevolucion(10L, Map.of("EFECTIVO", new BigDecimal("70.00"))));
    }

    @Test
    void cajaCerradaNoPermiteDevoluciones() {
        caja.setCierre(LocalDateTime.now(clock));
        caja.setClaveAbierta(null);
        assertThrows(ReglaNegocioException.class, () -> service.validarDevolucion(10L, Map.of("YAPE", new BigDecimal("100.00"))));
        verifyNoInteractions(metodos);
    }

    @Test
    void ventasPendientesImpidenElCierre() {
        usuario(RolUsuario.CAJERO, 2L);
        when(ventas.existsByCajaIdAndEstado(10L, EstadoVenta.PENDIENTE)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> service.cerrar(10L, new CerrarCajaRequestDTO(List.of(), null)));
        assertNull(caja.getCierre());
        verifyNoInteractions(detalles);
    }

    @Test
    void duenoNoPuedeCerrarLaCajaDeOtroUsuario() {
        usuario(RolUsuario.DUENO, 3L);
        assertThrows(AccessDeniedException.class, () -> service.cerrar(10L, new CerrarCajaRequestDTO(List.of(), null)));
        verifyNoInteractions(detalles);
    }

    @Test
    void actividadConNetoCeroSeGuardaEnElArqueo() {
        usuario(RolUsuario.CAJERO, 2L);
        when(cajas.sumarMovimientos(10L)).thenReturn(List.of(movimiento("YAPE", "0.00", 2L)));
        service.cerrar(10L, new CerrarCajaRequestDTO(List.of(), null));
        verify(detalles).save(argThat(d -> d.getMetodo().equals("YAPE") && d.getMontoEsperado().signum() == 0));
    }

    private void usuario(RolUsuario rol, Long id) {
        var u = new Usuario();
        u.setId(id);
        u.setRol(rol);
        u.setCorreo("prueba@example.invalid");
        when(contexto.usuario()).thenReturn(u);
    }

    private SesionCajaRepository.MovimientoCaja movimiento(String metodo, String monto, Long cantidad) {
        return new SesionCajaRepository.MovimientoCaja() {

            public String getMetodo() {
                return metodo;
            }

            public BigDecimal getMonto() {
                return new BigDecimal(monto);
            }

            public Long getMovimientos() {
                return cantidad;
            }
        };
    }
}
