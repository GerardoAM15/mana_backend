package pe.edu.upeu.mana.service.impl;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.*;
import pe.edu.upeu.mana.security.PermisosPorRol;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.venta.LineaVentaRequestDTO;
import pe.edu.upeu.mana.dto.comun.PagoRequestDTO;
import pe.edu.upeu.mana.dto.venta.PagosVentaRequestDTO;
import pe.edu.upeu.mana.dto.consumo.PlatoCantidadRequestDTO;
import pe.edu.upeu.mana.dto.venta.PlatoComboResponseDTO;
import pe.edu.upeu.mana.dto.pos.PlatoSueltoPosResponseDTO;
import pe.edu.upeu.mana.dto.pos.PosCatalogoResponseDTO;
import pe.edu.upeu.mana.dto.venta.VentaRequestDTO;
import pe.edu.upeu.mana.dto.venta.VentaResponseDTO;
import pe.edu.upeu.mana.enums.EstadoVenta;
import pe.edu.upeu.mana.enums.TipoCorrelativo;
import pe.edu.upeu.mana.enums.TipoLineaVenta;
import pe.edu.upeu.mana.enums.TipoMovimientoStock;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@Service
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    private static final java.util.Set<String> CAMPOS_ORDENABLES = java.util.Set.of("id", "numero", "fecha", "total", "estado");

    private final VentaRepository ventas;

    private final DetalleOrdenRepository detalles;

    private final DetalleComboPlatoRepository combos;

    private final PagoVentaRepository pagos;

    private final DevolucionVentaRepository devoluciones;

    private final ClienteRepository clientes;

    private final ServicioRepository servicios;

    private final PlatoRepository platos;

    private final BebidaRepository bebidas;

    private final ReglaCobroRepository reglas;

    private final ComponenteReglaRepository componentes;

    private final PrecioSueltoRepository precios;

    private final ProgramacionMenuRepository programaciones;

    private final MenuPlatoRepository menuPlatos;

    private final ContextoLocalService contexto;

    private final CajaService cajas;

    private final StockService stocks;

    private final CorrelativoService correlativos;

    private final ValidadorMenu validadorMenu;

    private final ValidadorPagos validadorPagos;

    private final CalculadoraTotalesVenta calculadora;

    private final VentaMapper mapper;

    private final ReglaCobroMapper reglaMapper;

    private final MenuService menus;

    private final ReservaComprobanteService comprobantes;

    private final Clock clock;

    private final ValidadorAtencion atencion;

    public VentaServiceImpl(VentaRepository ventas, DetalleOrdenRepository detalles, DetalleComboPlatoRepository combos, PagoVentaRepository pagos, DevolucionVentaRepository devoluciones, ClienteRepository clientes, ServicioRepository servicios, PlatoRepository platos, BebidaRepository bebidas, ReglaCobroRepository reglas, ComponenteReglaRepository componentes, PrecioSueltoRepository precios, ProgramacionMenuRepository programaciones, MenuPlatoRepository menuPlatos, ContextoLocalService contexto, CajaService cajas, StockService stocks, CorrelativoService correlativos, ValidadorMenu validadorMenu, ValidadorPagos validadorPagos, CalculadoraTotalesVenta calculadora, VentaMapper mapper, ReglaCobroMapper reglaMapper, MenuService menus, Clock clock, ValidadorAtencion atencion, ReservaComprobanteService comprobantes) {
        this.ventas = ventas;
        this.detalles = detalles;
        this.combos = combos;
        this.pagos = pagos;
        this.devoluciones = devoluciones;
        this.clientes = clientes;
        this.servicios = servicios;
        this.platos = platos;
        this.bebidas = bebidas;
        this.reglas = reglas;
        this.componentes = componentes;
        this.precios = precios;
        this.programaciones = programaciones;
        this.menuPlatos = menuPlatos;
        this.contexto = contexto;
        this.cajas = cajas;
        this.stocks = stocks;
        this.correlativos = correlativos;
        this.validadorMenu = validadorMenu;
        this.validadorPagos = validadorPagos;
        this.calculadora = calculadora;
        this.mapper = mapper;
        this.atencion = atencion;
        this.reglaMapper = reglaMapper;
        this.menus = menus;
        this.clock = clock;
        this.comprobantes = comprobantes;
    }

    @Transactional
    public VentaResponseDTO crear(VentaRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio VentaServiceImpl.crear");
        try {
            var caja = cajas.buscarActualParaActualizar();
            if (!servicios.findById(r.getServicio()).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
                throw new ReglaNegocioException("Servicio inexistente o inactivo");
            if (r.getClienteId() != null && !clientes.findById(r.getClienteId()).map(c -> Boolean.TRUE.equals(c.getActivo())).orElse(false))
                throw new ReglaNegocioException("Cliente inexistente o inactivo");
            atencion.validar(caja.getLocalId(), LocalDate.now(clock), r.getServicio());
            var ids = r.getLineas().stream().filter(l -> l.getTipo() == TipoLineaVenta.BEBIDA).map(LineaVentaRequestDTO::getBebidaId).collect(Collectors.toSet());
            var stock = stocks.bloquear(caja.getLocalId(), ids, false);
            var lineas = new ArrayList<DetalleOrden>();
            for (var linea : r.getLineas()) lineas.add(preparar(linea, r, caja.getLocalId()));
            var v = new Venta();
            v.setCajaId(caja.getId());
            v.setLocalId(caja.getLocalId());
            v.setClienteId(r.getClienteId());
            v.setServicio(r.getServicio());
            v.setParaLlevar(r.getParaLlevar());
            v.setEstado(EstadoVenta.PENDIENTE);
            v.setFecha(LocalDateTime.now(clock));
            v.setRecargoLlevar(calculadora.recargo(lineas));
            v.setTotal(calculadora.total(lineas));
            if (r.getPagos() != null && !r.getPagos().isEmpty())
                validadorPagos.validar(r.getPagos(), v.getTotal());
            v.setNumero(correlativos.siguiente(caja.getLocalId(), TipoCorrelativo.VENTA));
            ventas.saveAndFlush(v);
            for (int i = 0; i < lineas.size(); i++) {
                var d = lineas.get(i);
                d.setVentaId(v.getId());
                detalles.saveAndFlush(d);
                if (d.getTipo() == TipoLineaVenta.BEBIDA)
                    stocks.mover(stock.get(d.getBebidaId()), TipoMovimientoStock.VENTA, d.getCantidad(), null, d.getId(), "Venta " + v.getNumero());
                if (d.getTipo() == TipoLineaVenta.COMBO)
                    for (var p : r.getLineas().get(i).getPlatos().stream().sorted(Comparator.comparing(PlatoCantidadRequestDTO::getPlatoId)).toList()) {
                        var c = new DetalleComboPlato();
                        c.setDetalleId(d.getId());
                        c.setPlatoId(p.getPlatoId());
                        c.setCantidad(p.getCantidad());
                        combos.save(c);
                    }
            }
            if (r.getPagos() != null && !r.getPagos().isEmpty())
                guardarPagos(v, r.getPagos());
            if (v.getEstado() == EstadoVenta.PAGADA && r.getComprobante() != null) comprobantes.venta(v.getId(), r.getComprobante());
            ventas.flush();
            log.info("Venta registrada | numero={} | usuario={} | total={}", v.getNumero(), contexto.usuario().getCorreo(), v.getTotal());
            return respuesta(v);
        } finally {
            log.debug("Fin VentaServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private DetalleOrden preparar(LineaVentaRequestDTO l, VentaRequestDTO r, Long local) {
        if (!l.isFormaValida())
            throw new DatosInvalidosException("La línea no corresponde al tipo elegido");
        var d = new DetalleOrden();
        d.setTipo(l.getTipo());
        d.setCantidad(l.getCantidad());
        d.setRecargoUnitario(new BigDecimal("0.00"));
        switch(l.getTipo()) {
            case BEBIDA ->
                {
                    var b = bebidas.findById(l.getBebidaId()).orElseThrow(() -> new RecursoNoEncontradoException("Bebida no encontrada"));
                    if (!Boolean.TRUE.equals(b.getActivo()))
                        throw new ReglaNegocioException("La bebida está inactiva");
                    d.setBebidaId(b.getId());
                    d.setDescripcion(b.getNombre());
                    d.setPrecioUnitario(b.getPrecioVenta());
                }
            case PLATO ->
                {
                    var p = platos.findById(l.getPlatoId()).orElseThrow(() -> new RecursoNoEncontradoException("Plato no encontrado"));
                    if (!Boolean.TRUE.equals(p.getActivo()))
                        throw new ReglaNegocioException("El plato está inactivo");
                    var precio = precios.findById(new PrecioSueltoId(r.getServicio(), p.getCategoria())).filter(s -> Boolean.TRUE.equals(s.getActivo())).orElseThrow(() -> new ReglaNegocioException(r.getServicio().equals("C") ? "En la cena solo se vende el menú completo" : "No hay precio suelto activo para ese plato"));
                    validarProgramacion(local, r.getServicio(), p.getId());
                    d.setPlatoId(p.getId());
                    d.setDescripcion(p.getNombre());
                    d.setPrecioUnitario(precio.getPrecio());
                    if (r.getParaLlevar())
                        d.setRecargoUnitario(precio.getRecargoLlevar());
                }
            case COMBO ->
                {
                    var regla = reglas.findById(l.getReglaId()).orElseThrow(() -> new RecursoNoEncontradoException("Regla no encontrada"));
                    validadorMenu.validar(local, LocalDate.now(clock), r.getServicio(), regla, l.getPlatos());
                    d.setComboId(regla.getId());
                    d.setDescripcion(regla.getNombre());
                    d.setPrecioUnitario(regla.getPrecioCombo());
                    if (r.getParaLlevar())
                        d.setRecargoUnitario(regla.getRecargoLlevar());
                }
        }
        d.setPrecioUnitario(d.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP));
        d.setRecargoUnitario(d.getRecargoUnitario().setScale(2, RoundingMode.HALF_UP));
        return d;
    }

    private void validarProgramacion(Long local, String servicio, Long plato) {
        var m = programaciones.findByLocalIdAndFechaAndServicio(local, LocalDate.now(clock), servicio).orElseThrow(() -> new ReglaNegocioException("No hay menú programado para hoy"));
        if (!menuPlatos.existsById(new MenuPlatoId(m.getId(), plato)))
            throw new ReglaNegocioException("El plato no está en la programación de hoy");
    }

    @Transactional
    public VentaResponseDTO cobrar(Long id, PagosVentaRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio VentaServiceImpl.cobrar");
        try {
            bloquearCaja(id);
            var v = ventas.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada"));
            contexto.validarLocal(v.getLocalId());
            if (v.getEstado() != EstadoVenta.PENDIENTE)
                throw new ReglaNegocioException("Solo se pueden cobrar ventas pendientes");
            validadorPagos.validar(r.getPagos(), v.getTotal());
            guardarPagos(v, r.getPagos());
            if (r.getComprobante() != null) comprobantes.venta(v.getId(), r.getComprobante());
            ventas.flush();
            log.info("Venta cobrada | numero={} | usuario={} | total={}", v.getNumero(), contexto.usuario().getCorreo(), v.getTotal());
            return respuesta(v);
        } finally {
            log.debug("Fin VentaServiceImpl.cobrar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private void guardarPagos(Venta v, List<PagoRequestDTO> lineas) {
        for (var linea : lineas) {
            var p = new PagoVenta();
            p.setVentaId(v.getId());
            p.setMetodo(linea.getMetodo());
            p.setMonto(linea.getMonto().setScale(2, RoundingMode.HALF_UP));
            p.setFecha(LocalDateTime.now(clock));
            pagos.save(p);
        }
        v.setEstado(EstadoVenta.PAGADA);
    }

    @Transactional
    public VentaResponseDTO anular(Long id, AnularRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio VentaServiceImpl.anular");
        try {
            String motivo = r.getMotivo().trim();
            if (motivo.length() < 10 || motivo.length() > 250)
                throw new DatosInvalidosException("El motivo debe tener entre 10 y 250 caracteres sin espacios exteriores");
            bloquearCaja(id);
            var v = ventas.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada"));
            contexto.validarLocal(v.getLocalId());
            if (v.getEstado() == EstadoVenta.ANULADA)
                throw new ReglaNegocioException("La venta ya está anulada");
            var cobros = pagos.findByVentaIdOrderById(id);
            if (v.getEstado() == EstadoVenta.PAGADA) {
                var montos = new TreeMap<String, BigDecimal>();
                cobros.forEach(p -> montos.merge(p.getMetodo(), p.getMonto(), BigDecimal::add));
                cajas.validarDevolucion(v.getCajaId(), montos);
                for (var p : cobros) {
                    var d = new DevolucionVenta();
                    d.setVentaId(id);
                    d.setMetodo(p.getMetodo());
                    d.setMonto(p.getMonto());
                    d.setFecha(LocalDateTime.now(clock));
                    d.setMotivo(motivo);
                    devoluciones.save(d);
                }
            }
            var bebidasVendidas = detalles.findByVentaIdOrderById(id).stream().filter(d -> d.getTipo() == TipoLineaVenta.BEBIDA).toList();
            var stock = stocks.bloquear(v.getLocalId(), bebidasVendidas.stream().map(DetalleOrden::getBebidaId).collect(Collectors.toSet()), true);
            for (var d : bebidasVendidas) stocks.mover(stock.get(d.getBebidaId()), TipoMovimientoStock.DEVOLUCION, d.getCantidad(), null, d.getId(), "Anulación " + v.getNumero());
            comprobantes.anularVenta(id);
            v.setEstado(EstadoVenta.ANULADA);
            v.setAnuladaEn(LocalDateTime.now(clock));
            v.setMotivoAnulacion(motivo);
            ventas.flush();
            log.info("Venta anulada | numero={} | usuario={} | total={}", v.getNumero(), contexto.usuario().getCorreo(), v.getTotal());
            return respuesta(v);
        } finally {
            log.debug("Fin VentaServiceImpl.anular | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private void bloquearCaja(Long id) {
        var cajaId = ventas.buscarCajaId(id).orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada"));
        var caja = cajas.buscarAbiertaParaActualizar(cajaId);
        var usuario = contexto.usuario();
        if (!Objects.equals(caja.getUsuarioId(), usuario.getId()) && !PermisosPorRol.esAdministradorODueno(usuario.getRol()))
            throw new AccessDeniedException("Solo quien abrió la caja, un administrador o un dueño puede operar la venta");
    }

    @Transactional(readOnly = true)
    public VentaResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio VentaServiceImpl.buscar");
        try {
            var v = ventas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada"));
            contexto.validarLocal(v.getLocalId());
            return respuesta(v);
        } finally {
            log.debug("Fin VentaServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<VentaResponseDTO> listar(LocalDate desde, LocalDate hasta, EstadoVenta estado, Long cajaId, Long localId, int pagina, int tamanio, String ordenarPor, String direccion) {
        long inicio = System.nanoTime();
        log.debug("Inicio VentaServiceImpl.listar");
        try {
            var pageable = PaginacionUtil.construir(pagina, tamanio, ordenarPor, direccion, CAMPOS_ORDENABLES, "id");
            if (desde != null && hasta != null && desde.isAfter(hasta))
                throw new DatosInvalidosException("Rango de fechas inválido");
            return PaginaResponseDTO.de(ventas.filtrar(contexto.localId(localId), desde == null ? null : desde.atStartOfDay(), hasta == null ? null : hasta.plusDays(1).atStartOfDay(), estado, cajaId, pageable).map(this::respuesta));
        } finally {
            log.debug("Fin VentaServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicio) / 1_000_000);
        }
    }

    private VentaResponseDTO respuesta(Venta v) {
        var ordenes = detalles.findByVentaIdOrderById(v.getId());
        var seleccion = combos.findByDetalleIdInOrderByPlatoId(ordenes.stream().map(DetalleOrden::getId).toList());
        var catalogo = platos.findAllById(seleccion.stream().map(DetalleComboPlato::getPlatoId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(Plato::getId, p -> p));
        var porDetalle = seleccion.stream().collect(Collectors.groupingBy(DetalleComboPlato::getDetalleId));
        var lineas = ordenes.stream().map(d -> mapper.toLinea(d, porDetalle.getOrDefault(d.getId(), List.of()).stream().map(c -> new PlatoComboResponseDTO(c.getPlatoId(), catalogo.get(c.getPlatoId()).getNombre(), c.getCantidad())).toList())).toList();
        return mapper.toResponse(v, lineas, pagos.findByVentaIdOrderById(v.getId()), devoluciones.findByVentaIdOrderById(v.getId()));
    }

    @Transactional(readOnly = true)
    public PosCatalogoResponseDTO catalogo(String servicio) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio VentaServiceImpl.catalogo");
        try {
            if (!servicios.findById(servicio).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
                throw new ReglaNegocioException("Servicio inexistente o inactivo");
            Long local = contexto.localId(null);
            var hoy = LocalDate.now(clock);
            var menu = menus.listar(local, hoy, hoy, servicio).stream().findFirst().orElse(null);
            var sueltos = new ArrayList<PlatoSueltoPosResponseDTO>();
            if (menu != null)
                for (var p : menu.getPlatos()) {
                    if (!platos.findById(p.getPlatoId()).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
                        continue;
                    precios.findById(new PrecioSueltoId(servicio, p.getCategoria())).filter(s -> Boolean.TRUE.equals(s.getActivo())).ifPresent(s -> sueltos.add(new PlatoSueltoPosResponseDTO(p.getPlatoId(), p.getNombre(), p.getCategoria(), s.getPrecio(), s.getRecargoLlevar())));
                }
            var reglasPos = reglas.findByServicioAndActivoTrueOrderById(servicio).stream().map(r -> reglaMapper.toResponse(r, componentes.findByReglaId(r.getId()))).toList();
            return new PosCatalogoResponseDTO(local, hoy, servicio, menu, reglasPos, sueltos, bebidas.listarPos(local));
        } finally {
            log.debug("Fin VentaServiceImpl.catalogo | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
