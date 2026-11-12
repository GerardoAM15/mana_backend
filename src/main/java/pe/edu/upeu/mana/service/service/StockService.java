package pe.edu.upeu.mana.service.service;

import java.util.*;
import pe.edu.upeu.mana.entity.StockBebida;
import pe.edu.upeu.mana.enums.TipoMovimientoStock;

public interface StockService {

    Map<Long, StockBebida> bloquear(Long localId, Collection<Long> bebidaIds, boolean crear);

    void mover(StockBebida stock, TipoMovimientoStock tipo, long cantidad, Long detalleCompraId, Long detalleOrdenId, String motivo);
}
