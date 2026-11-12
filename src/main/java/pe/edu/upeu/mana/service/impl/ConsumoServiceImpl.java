package pe.edu.upeu.mana.service.impl;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.EstadoContrato;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.ConsumoMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.consumo.ConsumoRequestDTO;
import pe.edu.upeu.mana.dto.consumo.ConsumoResponseDTO;
import pe.edu.upeu.mana.dto.consumo.PlatoCantidadRequestDTO;

@Service
public class ConsumoServiceImpl implements ConsumoService {

    private static final Logger log = LoggerFactory.getLogger(ConsumoServiceImpl.class);

    private static final java.util.Set<String> CAMPOS_ORDENABLES = java.util.Set.of("id", "fecha", "registradoEn", "anulado");

    private final ContratoRepository contratos;

    private final DetalleContratoRepository detallesContrato;

    private final ConsumoRepository consumos;

    private final DetalleConsumoRepository detallesConsumo;

    private final PlatoRepository platos;

    private final ValidadorAtencion atencion;



    private final ServicioRepository servicios;

    private final MetodoPagoRepository metodos;

    private final ContextoLocalService contexto;

    private final CajaService cajas;

    private final ValidadorMenu menus;

    private final EstadoContratoActual estados;

    private final ConsumoMapper mapper;

    private final Clock clock;

    public ConsumoServiceImpl(ContratoRepository contratos, DetalleContratoRepository detallesContrato, ConsumoRepository consumos, DetalleConsumoRepository detallesConsumo, PlatoRepository platos, ValidadorAtencion atencion, ServicioRepository servicios, MetodoPagoRepository metodos, ContextoLocalService contexto, CajaService cajas, ValidadorMenu menus, EstadoContratoActual estados, ConsumoMapper mapper, Clock clock) {
        this.contratos = contratos;
        this.detallesContrato = detallesContrato;
        this.consumos = consumos;
        this.detallesConsumo = detallesConsumo;
        this.platos = platos;
        this.atencion = atencion;
        this.servicios = servicios;
        this.metodos = metodos;
        this.contexto = contexto;
        this.cajas = cajas;
        this.menus = menus;
        this.estados = estados;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public pe.edu.upeu.mana.dto.PaginaResponseDTO<ConsumoResponseDTO> listar(Long contratoId, String servicio, LocalDate desde, LocalDate hasta, Long localId, int pagina, int tamanio, String ordenarPor, String direccion) {
        long inicio = System.nanoTime();
        log.debug("Inicio ConsumoServiceImpl.listar");
        try {
            if (desde != null && hasta != null && desde.isAfter(hasta))
                throw new DatosInvalidosException("Rango de fechas inválido");
            var pageable = pe.edu.upeu.mana.util.PaginacionUtil.construir(pagina, tamanio, ordenarPor, direccion, CAMPOS_ORDENABLES, "id");
            var paginaConsumos = consumos.filtrar(contexto.localId(localId), contratoId, servicio, desde, hasta, pageable);
            var contratosMap = detallesContrato.findAllById(paginaConsumos.stream().map(Consumo::getDetalleContratoId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(DetalleContrato::getId, d -> d));
            var lineas = detallesConsumo.findByConsumoIdInOrderById(paginaConsumos.stream().map(Consumo::getId).toList());
            var catalogo = platos.findAllById(lineas.stream().map(DetalleConsumo::getPlatoId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(Plato::getId, p -> p));
            var porConsumo = lineas.stream().collect(Collectors.groupingBy(DetalleConsumo::getConsumoId));
            return pe.edu.upeu.mana.dto.PaginaResponseDTO.de(paginaConsumos.map(c -> mapper.toResponse(c, contratosMap.get(c.getDetalleContratoId()), porConsumo.getOrDefault(c.getId(), List.of()), catalogo)));
        } finally {
            log.debug("Fin ConsumoServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicio) / 1_000_000);
        }
    }

    @Transactional
    public ConsumoResponseDTO crear(ConsumoRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ConsumoServiceImpl.crear");
        try {
            var hoy = LocalDate.now(clock);
            Long local = contexto.localId(null);
            var regla = menus.obtenerRegla(r.getServicio());
            BigDecimal recargo = Boolean.TRUE.equals(r.getParaLlevar()) ? regla.getRecargoLlevar() : new BigDecimal("0.00");
            SesionCaja caja = null;
            if (recargo.signum() > 0) {
                if (r.getMetodoRecargo() == null || r.getMetodoRecargo().isBlank())
                    throw new ReglaNegocioException("Debe indicar el método de pago del recargo");
                if (!metodos.findById(r.getMetodoRecargo()).map(m -> Boolean.TRUE.equals(m.getActivo())).orElse(false))
                    throw new ReglaNegocioException("Método de pago inexistente o inactivo");
                caja = cajas.buscarActualParaActualizar();
                if (!caja.getLocalId().equals(local))
                    throw new ReglaNegocioException("La caja no corresponde al local");
            }
            var contrato = contratos.buscarParaActualizar(r.getContratoId()).orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
            contexto.validarLocal(contrato.getLocalId());
            if (!contrato.getLocalId().equals(local))
                throw new ReglaNegocioException("El contrato no corresponde al local");
            if (estados.calcular(contrato) != EstadoContrato.ACTIVO || hoy.isBefore(contrato.getFechaInicio()) || hoy.isAfter(contrato.getFechaFin()))
                throw new ReglaNegocioException("El contrato no está activo para hoy");
            atencion.validar(local, hoy, r.getServicio());
            var detalle = detallesContrato.buscarServicioParaActualizar(contrato.getId(), r.getServicio()).orElseThrow(() -> new ReglaNegocioException("El contrato no incluye ese servicio"));
            if (detalle.getSaldoRaciones() <= 0)
                throw new ReglaNegocioException("No quedan raciones para ese servicio");
            String clave = contrato.getClienteId() + ":" + hoy + ":" + r.getServicio();
            if (consumos.existsByClaveDiaria(clave)) {
                String nombre = servicios.findById(r.getServicio()).map(Servicio::getNombre).orElse(r.getServicio()).toLowerCase(Locale.ROOT);
                throw new ReglaNegocioException("Ya consumió " + (r.getServicio().equals("C") ? "la " : "el ") + nombre + " hoy");
            }
            var seleccion = menus.validar(local, hoy, r.getServicio(), regla, r.getPlatos());
            var c = new Consumo();
            c.setDetalleContratoId(detalle.getId());
            c.setFecha(hoy);
            c.setRegistradoEn(LocalDateTime.now(clock));
            c.setValorRacion(detalle.getPrecioRacionPactado());
            c.setClaveDiaria(clave);
            c.setParaLlevar(r.getParaLlevar());
            c.setRecargoLlevar(recargo);
            c.setAnulado(false);
            if (recargo.signum() > 0) {
                c.setMetodoRecargo(r.getMetodoRecargo());
                c.setCajaId(caja.getId());
            }
            detalle.setSaldoRaciones(detalle.getSaldoRaciones() - 1);
            consumos.saveAndFlush(c);
            var lineas = new ArrayList<DetalleConsumo>();
            for (var linea : r.getPlatos().stream().sorted(Comparator.comparing(PlatoCantidadRequestDTO::getPlatoId)).toList()) {
                var d = new DetalleConsumo();
                d.setConsumoId(c.getId());
                d.setPlatoId(linea.getPlatoId());
                d.setCantidad(linea.getCantidad());
                d.setCategoriaConsumida(seleccion.get(linea.getPlatoId()).getCategoria());
                lineas.add(detallesConsumo.save(d));
            }
            detallesConsumo.flush();
            log.info("Consumo registrado | contrato={} | consumo={} | usuario={} | recargo={}", contrato.getNumero(), c.getId(), contexto.usuario().getCorreo(), recargo);
            return mapper.toResponse(c, detalle, lineas, seleccion);
        } finally {
            log.debug("Fin ConsumoServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }


    @Transactional
    public ConsumoResponseDTO anular(Long id, AnularRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ConsumoServiceImpl.anular");
        try {
            String motivo = r.getMotivo().trim();
            if (motivo.length() < 10 || motivo.length() > 250)
                throw new DatosInvalidosException("El motivo debe tener entre 10 y 250 caracteres sin espacios exteriores");
            var ubicacion = consumos.buscarUbicacion(id).orElseThrow(() -> new RecursoNoEncontradoException("Consumo no encontrado"));
            if (ubicacion.getCajaId() != null)
                cajas.buscarAbiertaParaActualizar(ubicacion.getCajaId());
            var contrato = contratos.buscarParaActualizar(ubicacion.getContratoId()).orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
            contexto.validarLocal(contrato.getLocalId());
            var detalle = detallesContrato.buscarParaActualizar(ubicacion.getDetalleId()).orElseThrow(() -> new RecursoNoEncontradoException("Detalle no encontrado"));
            var c = consumos.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Consumo no encontrado"));
            if (Boolean.TRUE.equals(c.getAnulado()))
                throw new ReglaNegocioException("El consumo ya está anulado");
            if (!c.getFecha().equals(LocalDate.now(clock)))
                throw new ReglaNegocioException("Solo puede anular consumos de hoy");
            if (c.getRecargoLlevar().signum() > 0)
                cajas.validarDevolucion(c.getCajaId(), Map.of(c.getMetodoRecargo(), c.getRecargoLlevar()));
            c.setAnulado(true);
            c.setAnuladoEn(LocalDateTime.now(clock));
            c.setMotivoAnulacion(motivo);
            c.setClaveDiaria(null);
            detalle.setSaldoRaciones(detalle.getSaldoRaciones() + 1);
            consumos.flush();
            log.info("Consumo anulado | contrato={} | consumo={} | usuario={} | recargo={}", contrato.getNumero(), id, contexto.usuario().getCorreo(), c.getRecargoLlevar());
            return respuesta(c, detalle);
        } finally {
            log.debug("Fin ConsumoServiceImpl.anular | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<ConsumoResponseDTO> listarPorContrato(Long contratoId) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ConsumoServiceImpl.listarPorContrato");
        try {
            var contrato = contratos.findById(contratoId).orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
            contexto.validarLocal(contrato.getLocalId());
            var detalles = detallesContrato.findByContratoIdOrderById(contratoId).stream().collect(Collectors.toMap(DetalleContrato::getId, d -> d));
            return consumos.buscarPorContrato(contratoId).stream().map(c -> respuesta(c, detalles.get(c.getDetalleContratoId()))).toList();
        } finally {
            log.debug("Fin ConsumoServiceImpl.listarPorContrato | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private ConsumoResponseDTO respuesta(Consumo c, DetalleContrato d) {
        var lineas = detallesConsumo.findByConsumoIdOrderById(c.getId());
        var catalogo = platos.findAllById(lineas.stream().map(DetalleConsumo::getPlatoId).toList()).stream().collect(Collectors.toMap(Plato::getId, p -> p));
        return mapper.toResponse(c, d, lineas, catalogo);
    }
}
