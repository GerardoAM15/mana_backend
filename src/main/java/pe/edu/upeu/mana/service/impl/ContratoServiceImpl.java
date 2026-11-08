package pe.edu.upeu.mana.service.impl;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.ContratoMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoRequestDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.PensionistaResponseDTO;
import pe.edu.upeu.mana.dto.contrato.RacionesServicioResponseDTO;
import pe.edu.upeu.mana.dto.contrato.RenovarContratoRequestDTO;
import pe.edu.upeu.mana.dto.contrato.SimulacionContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.SimularContratoRequestDTO;
import pe.edu.upeu.mana.enums.EstadoContrato;
import pe.edu.upeu.mana.enums.TipoCorrelativo;
import pe.edu.upeu.mana.enums.TipoPagoContrato;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@Service
public class ContratoServiceImpl implements ContratoService {

    private static final Logger log = LoggerFactory.getLogger(ContratoServiceImpl.class);

    private static final java.util.Set<String> CAMPOS_ORDENABLES = java.util.Set.of("id", "numero", "fechaInicio", "fechaFin", "montoTotal", "estado");

    private final ContratoRepository contratos;

    private final ClienteRepository clientes;

    private final TipoPlanRepository planes;

    private final PlanServicioRepository planServicios;

    private final ServicioRepository servicios;

    private final ServicioDiaAtencionRepository dias;

    private final DiaNoAtencionRepository cierres;

    private final TarifaServicioRepository tarifas;

    private final DetalleContratoRepository detalles;

    private final PagoContratoRepository pagos;

    private final ConsumoRepository consumos;

    private final ContextoLocalService contexto;

    private final CajaService cajas;

    private final CalculadoraRaciones calculadora;

    private final CorrelativoService correlativos;

    private final ValidadorPagos validadorPagos;

    private final EstadoContratoActual estados;

    private final ConsumoService consultaConsumos;

    private final ContratoMapper mapper;

    private final Clock clock;

    public ContratoServiceImpl(ContratoRepository contratos, ClienteRepository clientes, TipoPlanRepository planes, PlanServicioRepository planServicios, ServicioRepository servicios, ServicioDiaAtencionRepository dias, DiaNoAtencionRepository cierres, TarifaServicioRepository tarifas, DetalleContratoRepository detalles, PagoContratoRepository pagos, ConsumoRepository consumos, ContextoLocalService contexto, CajaService cajas, CalculadoraRaciones calculadora, CorrelativoService correlativos, ValidadorPagos validadorPagos, EstadoContratoActual estados, ConsumoService consultaConsumos, ContratoMapper mapper, Clock clock) {
        this.contratos = contratos;
        this.clientes = clientes;
        this.planes = planes;
        this.planServicios = planServicios;
        this.servicios = servicios;
        this.dias = dias;
        this.cierres = cierres;
        this.tarifas = tarifas;
        this.detalles = detalles;
        this.pagos = pagos;
        this.consumos = consumos;
        this.contexto = contexto;
        this.cajas = cajas;
        this.calculadora = calculadora;
        this.correlativos = correlativos;
        this.validadorPagos = validadorPagos;
        this.estados = estados;
        this.consultaConsumos = consultaConsumos;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SimulacionContratoResponseDTO simular(SimularContratoRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContratoServiceImpl.simular");
        try {
            return calcular(r, contexto.localId(null));
        } finally {
            log.debug("Fin ContratoServiceImpl.simular | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private SimulacionContratoResponseDTO calcular(SimularContratoRequestDTO r, Long local) {
        var cliente = clientes.findById(r.getClienteId()).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        if (!Boolean.TRUE.equals(cliente.getActivo()))
            throw new ReglaNegocioException("El cliente está inactivo");
        var plan = planes.findById(r.getPlanId()).orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado"));
        if (!Boolean.TRUE.equals(plan.getActivo()))
            throw new ReglaNegocioException("El plan está inactivo");
        if (r.getFechaInicio().isBefore(LocalDate.now(clock)))
            throw new ReglaNegocioException("La fecha de inicio no puede ser anterior a hoy");
        if (r.getServicios() == null || r.getServicios().isEmpty() || new HashSet<>(r.getServicios()).size() != r.getServicios().size())
            throw new DatosInvalidosException("Seleccione servicios sin repetir");
        LocalDate fin = calculadora.fechaFin(r.getFechaInicio(), plan.getPeriodo());
        if (contratos.existeSuperposicion(cliente.getId(), r.getFechaInicio(), fin))
            throw new ReglaNegocioException("El cliente ya tiene un contrato con un periodo superpuesto");
        var permitidos = planServicios.findByPlanId(plan.getId()).stream().map(PlanServicio::getServicio).collect(Collectors.toSet());
        var feriados = cierres.filtrar(local, r.getFechaInicio(), fin).stream().map(DiaNoAtencion::getFecha).collect(Collectors.toSet());
        var lineas = new ArrayList<RacionesServicioResponseDTO>();
        BigDecimal total = new BigDecimal("0.00");
        for (String codigo : r.getServicios().stream().sorted().toList()) {
            if (!permitidos.contains(codigo))
                throw new ReglaNegocioException("El servicio no pertenece al plan");
            if (!servicios.findById(codigo).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
                throw new ReglaNegocioException("El servicio está inactivo");
            var calendario = dias.findByServicio(codigo).stream().map(ServicioDiaAtencion::getDiaSemana).collect(Collectors.toSet());
            long raciones = calculadora.calcular(r.getFechaInicio(), fin, calendario, feriados);
            if (raciones == 0)
                throw new ReglaNegocioException("El periodo no tiene raciones para el servicio " + codigo);
            var tarifa = tarifas.findByServicioAndActivoTrue(codigo).orElseThrow(() -> new ReglaNegocioException("No hay tarifa activa para el servicio " + codigo));
            BigDecimal precio = tarifa.getPrecio().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(raciones)).setScale(2, RoundingMode.HALF_UP);
            lineas.add(new RacionesServicioResponseDTO(codigo, raciones, precio, subtotal));
            total = total.add(subtotal);
        }
        if (total.compareTo(new BigDecimal("999999999999.99")) > 0)
            throw new ReglaNegocioException("El total supera el importe máximo permitido");
        return new SimulacionContratoResponseDTO(cliente.getId(), plan.getId(), r.getFechaInicio(), fin, lineas, total);
    }

    @Transactional
    public ContratoResponseDTO crear(ContratoRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContratoServiceImpl.crear");
        try {
            var caja = cajas.buscarActualParaActualizar();
            clientes.buscarParaActualizar(r.getClienteId()).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
            return registrar(r, caja, null);
        } finally {
            log.debug("Fin ContratoServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private ContratoResponseDTO registrar(ContratoRequestDTO r, SesionCaja caja, Long anterior) {
        var simulacion = calcular(new SimularContratoRequestDTO(r.getClienteId(), r.getPlanId(), r.getFechaInicio(), r.getServicios()), caja.getLocalId());
        validadorPagos.validar(r.getPagos(), simulacion.getTotal());
        var c = new Contrato();
        c.setClienteId(r.getClienteId());
        c.setPlanId(r.getPlanId());
        c.setLocalId(caja.getLocalId());
        c.setNumero(correlativos.siguiente(caja.getLocalId(), TipoCorrelativo.CONTRATO));
        c.setAnteriorId(anterior);
        c.setFechaInicio(r.getFechaInicio());
        c.setFechaFin(simulacion.getFechaFin());
        c.setMontoTotal(simulacion.getTotal());
        c.setEstado(r.getFechaInicio().equals(LocalDate.now(clock)) ? EstadoContrato.ACTIVO : EstadoContrato.PROGRAMADO);
        contratos.saveAndFlush(c);
        for (var linea : simulacion.getServicios()) {
            var d = new DetalleContrato();
            d.setContratoId(c.getId());
            d.setServicio(linea.getServicio());
            d.setRacionesContratadas(linea.getRaciones());
            d.setSaldoRaciones(linea.getRaciones());
            d.setPrecioRacionPactado(linea.getPrecioRacion());
            d.setSubtotal(linea.getSubtotal());
            detalles.save(d);
        }
        for (var pago : r.getPagos()) guardarPago(c.getId(), caja.getId(), TipoPagoContrato.COBRO, pago.getMetodo(), pago.getMonto(), pago.getReferencia());
        contratos.flush();
        log.info("Contrato registrado | numero={} | usuario={} | total={}", c.getNumero(), contexto.usuario().getCorreo(), c.getMontoTotal());
        return respuesta(c);
    }

    @Transactional
    public ContratoResponseDTO renovar(Long id, RenovarContratoRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContratoServiceImpl.renovar");
        try {
            var caja = cajas.buscarActualParaActualizar();
            Long clienteId = contratos.buscarClienteId(id).orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
            clientes.buscarParaActualizar(clienteId).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
            var anterior = contratos.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
            contexto.validarLocal(anterior.getLocalId());
            if (!anterior.getLocalId().equals(caja.getLocalId()))
                throw new ReglaNegocioException("Debe renovar en el local del contrato");
            LocalDate hoy = LocalDate.now(clock);
            if (estados.calcular(anterior) != EstadoContrato.ACTIVO)
                throw new ReglaNegocioException("Solo puede renovar un contrato activo");
            if (hoy.isBefore(anterior.getFechaFin().minusDays(3)))
                throw new ReglaNegocioException("Solo puede renovar dentro de los últimos 3 días del contrato");
            if (contratos.existsByAnteriorIdAndEstadoNot(id, EstadoContrato.ANULADO))
                throw new ReglaNegocioException("El contrato ya tiene una renovación vigente");
            LocalDate inicio = anterior.getFechaFin().plusDays(1);
            if (inicio.isBefore(hoy))
                inicio = hoy;
            return registrar(new ContratoRequestDTO(clienteId, r.getPlanId(), inicio, r.getServicios(), r.getPagos()), caja, id);
        } finally {
            log.debug("Fin ContratoServiceImpl.renovar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public ContratoResponseDTO anular(Long id, AnularRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContratoServiceImpl.anular");
        try {
            String motivo = validarMotivo(r);
            var caja = cajas.buscarActualParaActualizar();
            Long cliente = contratos.buscarClienteId(id).orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
            clientes.buscarParaActualizar(cliente).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
            var c = contratos.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
            contexto.validarLocal(c.getLocalId());
            if (!c.getLocalId().equals(caja.getLocalId()))
                throw new ReglaNegocioException("La caja y el contrato deben ser del mismo local");
            var estado = estados.calcular(c);
            if (estado != EstadoContrato.ACTIVO && estado != EstadoContrato.PROGRAMADO)
                throw new ReglaNegocioException("Solo se anulan contratos programados o activos");
            if (consumos.existePorContrato(id))
                throw new ReglaNegocioException("No se puede anular un contrato que tiene consumos, incluso anulados");
            var cobros = pagos.findByContratoIdOrderById(id).stream().filter(p -> p.getTipo() == TipoPagoContrato.COBRO).toList();
            var montos = new TreeMap<String, BigDecimal>();
            cobros.forEach(p -> montos.merge(p.getMetodo(), p.getMonto(), BigDecimal::add));
            cajas.validarDevolucion(caja.getId(), montos);
            for (var p : cobros) guardarPago(id, caja.getId(), TipoPagoContrato.DEVOLUCION, p.getMetodo(), p.getMonto(), p.getReferencia());
            c.setEstado(EstadoContrato.ANULADO);
            c.setAnuladoEn(LocalDateTime.now(clock));
            c.setMotivoAnulacion(motivo);
            contratos.flush();
            log.info("Contrato anulado | numero={} | usuario={} | devolucion={}", c.getNumero(), contexto.usuario().getCorreo(), c.getMontoTotal());
            return respuesta(c);
        } finally {
            log.debug("Fin ContratoServiceImpl.anular | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private String validarMotivo(AnularRequestDTO r) {
        String motivo = r.getMotivo().trim();
        if (motivo.length() < 10 || motivo.length() > 250)
            throw new DatosInvalidosException("El motivo debe tener entre 10 y 250 caracteres sin espacios exteriores");
        return motivo;
    }

    private void guardarPago(Long contrato, Long caja, TipoPagoContrato tipo, String metodo, BigDecimal monto, String referencia) {
        var p = new PagoContrato();
        p.setContratoId(contrato);
        p.setCajaId(caja);
        p.setTipo(tipo);
        p.setMetodo(metodo);
        p.setMonto(monto.setScale(2, RoundingMode.HALF_UP));
        p.setFecha(LocalDateTime.now(clock));
        p.setReferencia(referencia);
        pagos.save(p);
    }

    @Transactional(readOnly = true)
    public ContratoResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContratoServiceImpl.buscar");
        try {
            var c = obtener(id);
            contexto.validarLocal(c.getLocalId());
            return respuesta(c);
        } finally {
            log.debug("Fin ContratoServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ContratoResponseDTO> listar(String dni, EstadoContrato estado, LocalDate desde, LocalDate hasta, Long localId, int pagina, int tamanio, String ordenarPor, String direccion) {
        long inicio = System.nanoTime();
        log.debug("Inicio ContratoServiceImpl.listar");
        try {
            var pageable = PaginacionUtil.construir(pagina, tamanio, ordenarPor, direccion, CAMPOS_ORDENABLES, "id");
            if (desde != null && hasta != null && desde.isAfter(hasta))
                throw new DatosInvalidosException("Rango de fechas inválido");
            var paginaContratos = contratos.filtrar(contexto.localId(localId), dni == null || dni.isBlank() ? null : dni.trim(), desde, hasta, pageable, estado == null ? null : estado.name(), LocalDate.now(clock));
            var clienteMap = clientes.findAllById(paginaContratos.stream().map(Contrato::getClienteId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(Cliente::getId, c -> c));
            var planMap = planes.findAllById(paginaContratos.stream().map(Contrato::getPlanId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(TipoPlan::getId, p -> p));
            var detalleMap = detalles.findByContratoIdInOrderById(paginaContratos.stream().map(Contrato::getId).toList()).stream().collect(Collectors.groupingBy(DetalleContrato::getContratoId));
            return PaginaResponseDTO.de(paginaContratos.map(c -> mapper.toResponse(c, clienteMap.get(c.getClienteId()), planMap.get(c.getPlanId()), detalleMap.getOrDefault(c.getId(), List.of()), List.of(), List.of())));
        } finally {
            log.debug("Fin ContratoServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicio) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public ContratoResponseDTO buscarVigente(Long clienteId, Long localId) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContratoServiceImpl.buscarVigente");
        try {
            if (!clientes.existsById(clienteId))
                throw new RecursoNoEncontradoException("Cliente no encontrado");
            return respuesta(contratos.buscarVigente(clienteId, contexto.localId(localId), LocalDate.now(clock)).orElseThrow(() -> new RecursoNoEncontradoException("El cliente no tiene un contrato vigente en el local")));
        } finally {
            log.debug("Fin ContratoServiceImpl.buscarVigente | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<PensionistaResponseDTO> buscarPensionistas(String q, Long localId) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContratoServiceImpl.buscarPensionistas");
        try {
            if (q == null || q.isBlank())
                throw new DatosInvalidosException("Escriba el DNI o nombre para buscar");
            String texto = "%" + q.trim().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            var encontrados = contratos.buscarPensionistas(contexto.localId(localId), LocalDate.now(clock), texto);
            var clienteMap = clientes.findAllById(encontrados.stream().map(Contrato::getClienteId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(Cliente::getId, c -> c));
            var detalleMap = detalles.findByContratoIdInOrderById(encontrados.stream().map(Contrato::getId).toList()).stream().collect(Collectors.groupingBy(DetalleContrato::getContratoId));
            return encontrados.stream().map(c -> mapper.toPensionista(c, clienteMap.get(c.getClienteId()), detalleMap.getOrDefault(c.getId(), List.of()))).toList();
        } finally {
            log.debug("Fin ContratoServiceImpl.buscarPensionistas | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void actualizarEstados() {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContratoServiceImpl.actualizarEstados");
        try {
            LocalDate hoy = LocalDate.now(clock);
            var ahora = OffsetDateTime.now(clock);
            int finalizados = contratos.finalizarVencidos(hoy, ahora);
            int activados = contratos.activarProgramados(hoy, ahora);
            if (finalizados + activados > 0)
                log.info("Estados de contratos actualizados | finalizados={} | activados={}", finalizados, activados);
        } finally {
            log.debug("Fin ContratoServiceImpl.actualizarEstados | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private Contrato obtener(Long id) {
        return contratos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
    }

    private ContratoResponseDTO respuesta(Contrato c) {
        return mapper.toResponse(c, clientes.findById(c.getClienteId()).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado")), planes.findById(c.getPlanId()).orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado")), detalles.findByContratoIdOrderById(c.getId()), pagos.findByContratoIdOrderById(c.getId()), consultaConsumos.listarPorContrato(c.getId()));
    }
}
