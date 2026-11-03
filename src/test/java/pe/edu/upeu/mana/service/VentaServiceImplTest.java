package pe.edu.upeu.mana.service;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.TipoLineaVenta;
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
}
