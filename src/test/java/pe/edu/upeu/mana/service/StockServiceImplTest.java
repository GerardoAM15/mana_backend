package pe.edu.upeu.mana.service;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.mana.entity.StockBebida;
import pe.edu.upeu.mana.enums.TipoMovimientoStock;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.impl.StockServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockServiceImplTest {

    private final MovimientoStockRepository movimientos = mock(MovimientoStockRepository.class);

    private final StockServiceImpl service = new StockServiceImpl(mock(BebidaRepository.class), mock(StockBebidaRepository.class), movimientos, Clock.systemUTC());

    @Test
    void noVendeMasDelStockDisponible() {
        var s = new StockBebida();
        s.setStock(2L);
        assertThrows(ReglaNegocioException.class, () -> service.mover(s, TipoMovimientoStock.VENTA, 3, null, 1L, "Venta"));
        assertEquals(2L, s.getStock());
        verifyNoInteractions(movimientos);
    }

    @Test
    void compraNoPuedeDesbordarLaColumnaOracle() {
        var s = new StockBebida();
        s.setStock(9999999999L);
        assertThrows(ReglaNegocioException.class, () -> service.mover(s, TipoMovimientoStock.COMPRA, 1, 1L, null, "Compra"));
        assertEquals(9999999999L, s.getStock());
        verifyNoInteractions(movimientos);
    }
}
