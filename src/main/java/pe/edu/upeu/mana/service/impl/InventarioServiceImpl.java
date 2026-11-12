package pe.edu.upeu.mana.service.impl;

import java.math.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.TipoMovimientoStock;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.mapper.InventarioMapper;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.inventario.CompraBebidaRequestDTO;
import pe.edu.upeu.mana.dto.inventario.CompraBebidaResponseDTO;
import pe.edu.upeu.mana.dto.inventario.DetalleCompraRequestDTO;
import pe.edu.upeu.mana.dto.inventario.MovimientoStockResponseDTO;
import pe.edu.upeu.mana.dto.inventario.StockBebidaResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@Service
public class InventarioServiceImpl implements InventarioService {

    private static final Logger log = LoggerFactory.getLogger(InventarioServiceImpl.class);

    private static final java.util.Set<String> CAMPOS_ORDENABLES = java.util.Set.of("id", "fecha", "cantidad", "tipo", "bebidaId");

    private final CompraBebidaRepository compras;

    private final DetalleCompraRepository detalles;

    private final ProveedorRepository proveedores;

    private final ProveedorBebidaRepository relaciones;

    private final BebidaRepository bebidas;

    private final StockBebidaRepository stockRepository;

    private final MovimientoStockRepository movimientos;

    private final StockService stocks;

    private final ContextoLocalService contexto;

    private final InventarioMapper mapper;

    public InventarioServiceImpl(CompraBebidaRepository compras, DetalleCompraRepository detalles, ProveedorRepository proveedores, ProveedorBebidaRepository relaciones, BebidaRepository bebidas, StockBebidaRepository stockRepository, MovimientoStockRepository movimientos, StockService stocks, ContextoLocalService contexto, InventarioMapper mapper) {
        this.compras = compras;
        this.detalles = detalles;
        this.proveedores = proveedores;
        this.relaciones = relaciones;
        this.bebidas = bebidas;
        this.stockRepository = stockRepository;
        this.movimientos = movimientos;
        this.stocks = stocks;
        this.contexto = contexto;
        this.mapper = mapper;
    }

    @Transactional
    public CompraBebidaResponseDTO crear(CompraBebidaRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio InventarioServiceImpl.crear");
        try {
            Long local = contexto.localId(null);
            var proveedor = proveedores.buscarParaActualizar(r.getProveedorId()).orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado"));
            if (!Boolean.TRUE.equals(proveedor.getActivo()))
                throw new ReglaNegocioException("El proveedor está inactivo");
            var disponibles = relaciones.findByProveedorId(proveedor.getId()).stream().map(ProveedorBebida::getBebidaId).collect(Collectors.toSet());
            var ids = r.getDetalles().stream().map(DetalleCompraRequestDTO::getBebidaId).collect(Collectors.toSet());
            if (!disponibles.containsAll(ids))
                throw new ReglaNegocioException("La bebida no pertenece al proveedor");
            var stock = stocks.bloquear(local, ids, true);
            BigDecimal total = new BigDecimal("0.00");
            for (var d : r.getDetalles()) {
                if (!bebidas.findById(d.getBebidaId()).map(b -> Boolean.TRUE.equals(b.getActivo())).orElse(false))
                    throw new ReglaNegocioException("La bebida está inactiva");
                unidades(d);
                total = total.add(d.getCostoTotal());
            }
            if (total.compareTo(new BigDecimal("999999999999.99")) > 0)
                throw new ReglaNegocioException("El total supera el importe máximo permitido");
            var compra = new CompraBebida();
            compra.setLocalId(local);
            compra.setProveedorId(proveedor.getId());
            compra.setFecha(r.getFecha());
            compra.setTotal(total.setScale(2, RoundingMode.HALF_UP));
            compras.saveAndFlush(compra);
            for (var linea : r.getDetalles()) {
                var d = new DetalleCompra();
                d.setCompraId(compra.getId());
                d.setBebidaId(linea.getBebidaId());
                d.setPresentaciones(linea.getPresentaciones());
                d.setUnidadesPorPresentacion(linea.getUnidadesPorPresentacion());
                d.setCostoTotal(linea.getCostoTotal().setScale(2, RoundingMode.HALF_UP));
                detalles.saveAndFlush(d);
                stocks.mover(stock.get(d.getBebidaId()), TipoMovimientoStock.COMPRA, unidades(linea), d.getId(), null, "Compra " + compra.getId());
            }
            compras.flush();
            log.info("Compra registrada | compra={} | usuario={} | total={}", compra.getId(), contexto.usuario().getCorreo(), total);
            return respuesta(compra);
        } finally {
            log.debug("Fin InventarioServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private long unidades(DetalleCompraRequestDTO d) {
        try {
            long n = Math.multiplyExact(d.getPresentaciones(), d.getUnidadesPorPresentacion());
            if (n <= 0 || n > 9999999999L)
                throw new ArithmeticException();
            return n;
        } catch (ArithmeticException ex) {
            throw new ReglaNegocioException("Las unidades de compra superan el máximo permitido");
        }
    }

    @Transactional(readOnly = true)
    public CompraBebidaResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio InventarioServiceImpl.buscar");
        try {
            var c = compras.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Compra no encontrada"));
            contexto.validarLocal(c.getLocalId());
            return respuesta(c);
        } finally {
            log.debug("Fin InventarioServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<CompraBebidaResponseDTO> listar(Long local, LocalDate desde, LocalDate hasta) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio InventarioServiceImpl.listar");
        try {
            validarRango(desde, hasta);
            return compras.filtrar(contexto.localId(local), desde, hasta).stream().map(this::respuesta).toList();
        } finally {
            log.debug("Fin InventarioServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<StockBebidaResponseDTO> listarStock(Long local) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio InventarioServiceImpl.listarStock");
        try {
            return stockRepository.listarStock(contexto.localId(local));
        } finally {
            log.debug("Fin InventarioServiceImpl.listarStock | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<MovimientoStockResponseDTO> listarMovimientos(Long localId, Long bebidaId, LocalDate desde, LocalDate hasta, int pagina, int tamanio, String ordenarPor, String direccion) {
        long inicio = System.nanoTime();
        log.debug("Inicio InventarioServiceImpl.listarMovimientos");
        try {
            var pageable = PaginacionUtil.construir(pagina, tamanio, ordenarPor, direccion, CAMPOS_ORDENABLES, "id");
            validarRango(desde, hasta);
            return PaginaResponseDTO.de(movimientos.filtrar(contexto.localId(localId), bebidaId, desde == null ? null : desde.atStartOfDay(), hasta == null ? null : hasta.plusDays(1).atStartOfDay(), pageable));
        } finally {
            log.debug("Fin InventarioServiceImpl.listarMovimientos | duracionMs={}", (System.nanoTime() - inicio) / 1_000_000);
        }
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw new DatosInvalidosException("Rango de fechas inválido");
    }

    private CompraBebidaResponseDTO respuesta(CompraBebida c) {
        var proveedor = proveedores.findById(c.getProveedorId()).orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado"));
        return mapper.toResponse(c, proveedor.getRazonSocial(), detalles.findByCompraIdOrderById(c.getId()).stream().map(d -> mapper.toDetalle(d, bebidas.findById(d.getBebidaId()).orElseThrow(() -> new RecursoNoEncontradoException("Bebida no encontrada")).getNombre())).toList());
    }
}
