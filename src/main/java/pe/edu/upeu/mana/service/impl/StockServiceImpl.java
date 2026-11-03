package pe.edu.upeu.mana.service.impl;

import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.TipoMovimientoStock;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.service.service.StockService;

@Service
public class StockServiceImpl implements StockService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StockServiceImpl.class);

    private final BebidaRepository bebidas;

    private final StockBebidaRepository stocks;

    private final MovimientoStockRepository movimientos;

    private final Clock clock;

    public StockServiceImpl(BebidaRepository bebidas, StockBebidaRepository stocks, MovimientoStockRepository movimientos, Clock clock) {
        this.bebidas = bebidas;
        this.stocks = stocks;
        this.movimientos = movimientos;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Map<Long, StockBebida> bloquear(Long localId, Collection<Long> ids, boolean crear) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio StockServiceImpl.bloquear");
        try {
            var resultado = new TreeMap<Long, StockBebida>();
            // Los bloqueos de bebida serializan la creación de filas de stock inexistentes.
            for (Long id : new TreeSet<>(ids)) bebidas.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Bebida no encontrada"));
            if (ids.isEmpty())
                return resultado;
            for (var s : stocks.findByLocalIdAndBebidaIdInOrderById(localId, ids)) {
                var bloqueado = stocks.buscarParaActualizar(s.getId()).orElseThrow(() -> new RecursoNoEncontradoException("Stock no encontrado"));
                resultado.put(s.getBebidaId(), bloqueado);
            }
            for (Long id : new TreeSet<>(ids)) if (!resultado.containsKey(id)) {
                if (!crear)
                    throw new ReglaNegocioException("No hay stock de la bebida en el local");
                var s = new StockBebida();
                s.setLocalId(localId);
                s.setBebidaId(id);
                s.setStock(0L);
                stocks.saveAndFlush(s);
                resultado.put(id, s);
            }
            return resultado;
        } finally {
            log.info("Fin StockServiceImpl.bloquear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void mover(StockBebida stock, TipoMovimientoStock tipo, long cantidad, Long compra, Long orden, String motivo) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio StockServiceImpl.mover");
        try {
            if (cantidad <= 0 || cantidad > 9999999999L)
                throw new ReglaNegocioException("Cantidad de stock fuera de rango");
            long nuevo = stock.getStock() + (tipo == TipoMovimientoStock.VENTA ? -cantidad : cantidad);
            if (nuevo < 0)
                throw new ReglaNegocioException("Stock insuficiente de la bebida");
            if (nuevo > 9999999999L)
                throw new ReglaNegocioException("El stock supera el máximo permitido");
            stock.setStock(nuevo);
            var m = new MovimientoStock();
            m.setLocalId(stock.getLocalId());
            m.setBebidaId(stock.getBebidaId());
            m.setTipo(tipo);
            m.setCantidad(cantidad);
            m.setFecha(LocalDateTime.now(clock));
            m.setDetalleCompraId(compra);
            m.setDetalleOrdenId(orden);
            m.setMotivo(motivo);
            movimientos.save(m);
        } finally {
            log.info("Fin StockServiceImpl.mover | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
