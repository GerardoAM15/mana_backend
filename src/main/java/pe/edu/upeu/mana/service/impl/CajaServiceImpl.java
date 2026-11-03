package pe.edu.upeu.mana.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.RolUsuario;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.CajaMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.security.PermisosPorRol;
import pe.edu.upeu.mana.service.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.caja.AbrirCajaRequestDTO;
import pe.edu.upeu.mana.dto.caja.CajaResponseDTO;
import pe.edu.upeu.mana.dto.caja.CerrarCajaRequestDTO;
import pe.edu.upeu.mana.dto.caja.MetodoCajaResponseDTO;
import pe.edu.upeu.mana.dto.caja.ResumenCajaResponseDTO;
import pe.edu.upeu.mana.enums.EstadoVenta;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@Service
public class CajaServiceImpl implements CajaService {

    private static final Logger log = LoggerFactory.getLogger(CajaServiceImpl.class);

    private static final BigDecimal CERO = new BigDecimal("0.00");

    private static final java.util.Set<String> CAMPOS_ORDENABLES = java.util.Set.of("id", "apertura", "cierre", "usuarioId", "montoInicial");

    private final SesionCajaRepository cajas;

    private final UsuarioRepository usuarios;

    private final VentaRepository ventas;

    private final DetalleCierreCajaRepository detalles;

    private final MetodoPagoRepository metodos;

    private final ContextoLocalService contexto;

    private final CajaMapper mapper;

    private final Clock clock;

    public CajaServiceImpl(SesionCajaRepository cajas, UsuarioRepository usuarios, VentaRepository ventas, DetalleCierreCajaRepository detalles, MetodoPagoRepository metodos, ContextoLocalService contexto, CajaMapper mapper, Clock clock) {
        this.cajas = cajas;
        this.usuarios = usuarios;
        this.ventas = ventas;
        this.detalles = detalles;
        this.metodos = metodos;
        this.contexto = contexto;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional
    public CajaResponseDTO abrir(AbrirCajaRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CajaServiceImpl.abrir");
        try {
            var usuario = contexto.usuario();
            // Serializa aperturas concurrentes; la clave única protege también a otros procesos.
            usuarios.buscarParaActualizar(usuario.getId()).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            if (cajas.findByClaveAbierta(usuario.getId().toString()).isPresent())
                throw new ReglaNegocioException("Ya tiene una caja abierta");
            var caja = new SesionCaja();
            caja.setLocalId(contexto.localId(null));
            caja.setUsuarioId(usuario.getId());
            caja.setMontoInicial(dinero(r.getMontoInicial()));
            caja.setApertura(LocalDateTime.now(clock));
            caja.setClaveAbierta(usuario.getId().toString());
            cajas.saveAndFlush(caja);
            log.info("Caja abierta | caja={} | usuario={} | montoInicial={}", caja.getId(), usuario.getCorreo(), caja.getMontoInicial());
            return mapper.toResponse(caja);
        } finally {
            log.info("Fin CajaServiceImpl.abrir | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public CajaResponseDTO buscarActual() {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CajaServiceImpl.buscarActual");
        try {
            return mapper.toResponse(actual());
        } finally {
            log.info("Fin CajaServiceImpl.buscarActual | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public ResumenCajaResponseDTO resumir(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CajaServiceImpl.resumir");
        try {
            var caja = obtener(id);
            validarLectura(caja);
            var saldos = calcular(caja);
            var contados = new HashMap<String, DetalleCierreCaja>();
            if (caja.getCierre() != null)
                detalles.findBySesionCajaIdOrderByMetodo(caja.getId()).forEach(d -> contados.put(d.getMetodo(), d));
            var lineas = saldos.values().stream().map(s -> {
                var d = contados.get(s.getMetodo());
                return new MetodoCajaResponseDTO(s.getMetodo(), s.getEsperado(), d == null ? null : d.getMontoContado(), d == null ? null : d.getDiferencia(), s.getMovimientos());
            }).toList();
            var conteos = cajas.contarOperaciones(id);
            return new ResumenCajaResponseDTO(mapper.toResponse(caja), lineas, total(saldos), conteos.getVentas(), conteos.getConsumos(), conteos.getContratos());
        } finally {
            log.info("Fin CajaServiceImpl.resumir | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public CajaResponseDTO cerrar(Long id, CerrarCajaRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CajaServiceImpl.cerrar");
        try {
            var caja = cajas.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Caja no encontrada"));
            contexto.validarLocal(caja.getLocalId());
            var usuario = contexto.usuario();
            if (!caja.getUsuarioId().equals(usuario.getId()) && usuario.getRol() != RolUsuario.ADMINISTRADOR)
                throw new AccessDeniedException("Solo el dueño de la caja o un administrador puede cerrarla");
            exigirAbierta(caja);
            if (ventas.existsByCajaIdAndEstado(id, pe.edu.upeu.mana.enums.EstadoVenta.PENDIENTE))
                throw new ReglaNegocioException("No se puede cerrar la caja con ventas pendientes");
            var saldos = calcular(caja);
            var contados = new HashMap<String, BigDecimal>();
            for (var conteo : r.getConteos()) {
                if (conteo.getMontoContado().signum() < 0)
                    throw new DatosInvalidosException("El monto contado no puede ser negativo");
                if (contados.putIfAbsent(conteo.getMetodo(), dinero(conteo.getMontoContado())) != null)
                    throw new DatosInvalidosException("El método de pago está repetido");
                if (!saldos.containsKey(conteo.getMetodo()))
                    throw new DatosInvalidosException("Método de pago no registrado");
            }
            BigDecimal totalContado = CERO;
            for (var saldo : saldos.values()) {
                // Omitir un método equivale a contar cero; se conserva cualquier movimiento, aunque su neto sea cero.
                BigDecimal contado = contados.getOrDefault(saldo.getMetodo(), CERO);
                if (saldo.getMovimientos() > 0 || contado.signum() > 0) {
                    var d = new DetalleCierreCaja();
                    d.setSesionCajaId(id);
                    d.setMetodo(saldo.getMetodo());
                    d.setMontoEsperado(saldo.getEsperado());
                    d.setMontoContado(contado);
                    d.setDiferencia(contado.subtract(saldo.getEsperado()));
                    detalles.save(d);
                }
                totalContado = totalContado.add(contado);
            }
            caja.setMontoEsperado(total(saldos));
            caja.setMontoContado(totalContado);
            caja.setDiferencia(totalContado.subtract(caja.getMontoEsperado()));
            caja.setObservacion(r.getObservacion());
            caja.setCierre(LocalDateTime.now(clock));
            caja.setClaveAbierta(null);
            cajas.flush();
            log.info("Caja cerrada | caja={} | usuario={} | esperado={} | contado={} | diferencia={}", id, usuario.getCorreo(), caja.getMontoEsperado(), totalContado, caja.getDiferencia());
            return mapper.toResponse(caja);
        } finally {
            log.info("Fin CajaServiceImpl.cerrar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<CajaResponseDTO> listar(LocalDate desde, LocalDate hasta, Long usuarioId, Long localId, int pagina, int tamanio, String ordenarPor, String direccion) {
        long inicio = System.nanoTime();
        log.info("Inicio CajaServiceImpl.listar");
        try {
            var pageable = PaginacionUtil.construir(pagina, tamanio, ordenarPor, direccion, CAMPOS_ORDENABLES, "id");
            if (!pe.edu.upeu.mana.security.PermisosPorRol.permisos(contexto.usuario().getRol().name()).contains("CAJA_SUPERVISAR"))
                throw new org.springframework.security.access.AccessDeniedException("No tiene permiso para supervisar cajas");
            if (desde != null && hasta != null && desde.isAfter(hasta))
                throw new DatosInvalidosException("Rango de fechas inválido");
            return PaginaResponseDTO.de(cajas.filtrar(contexto.localId(localId), usuarioId, desde == null ? null : desde.atStartOfDay(), hasta == null ? null : hasta.plusDays(1).atStartOfDay(), pageable).map(mapper::toResponse));
        } finally {
            log.info("Fin CajaServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicio) / 1_000_000);
        }
    }

    @Transactional
    public SesionCaja buscarActualParaActualizar() {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CajaServiceImpl.buscarActualParaActualizar");
        try {
            var usuario = contexto.usuario();
            var caja = cajas.buscarAbiertaParaActualizar(usuario.getId().toString()).orElseThrow(() -> new ReglaNegocioException("Debe tener una caja abierta"));
            contexto.validarLocal(caja.getLocalId());
            exigirAbierta(caja);
            return caja;
        } finally {
            log.info("Fin CajaServiceImpl.buscarActualParaActualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public SesionCaja buscarAbiertaParaActualizar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CajaServiceImpl.buscarAbiertaParaActualizar");
        try {
            var caja = cajas.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Caja no encontrada"));
            contexto.validarLocal(caja.getLocalId());
            exigirAbierta(caja);
            return caja;
        } finally {
            log.info("Fin CajaServiceImpl.buscarAbiertaParaActualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void validarDevolucion(Long cajaId, Map<String, BigDecimal> montosPorMetodo) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CajaServiceImpl.validarDevolucion");
        try {
            // El bloqueo se conserva hasta el fin de la transacción de venta o contrato que efectúa la devolución.
            var caja = buscarAbiertaParaActualizar(cajaId);
            for (var entrada : montosPorMetodo.entrySet()) {
                if (entrada.getValue() == null || entrada.getValue().signum() < 0)
                    throw new DatosInvalidosException("Monto de devolución inválido");
                if (!metodos.existsById(entrada.getKey()))
                    throw new DatosInvalidosException("Método de pago no registrado");
            }
            var efectivo = montosPorMetodo.getOrDefault("EFECTIVO", CERO);
            if (efectivo.signum() > 0 && calcular(caja).get("EFECTIVO").getEsperado().compareTo(efectivo) < 0)
                throw new ReglaNegocioException("No hay efectivo suficiente en caja para la devolución");
        } finally {
            log.info("Fin CajaServiceImpl.validarDevolucion | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private Map<String, Saldo> calcular(SesionCaja caja) {
        var saldos = new TreeMap<String, Saldo>();
        for (var metodo : metodos.findAll()) saldos.put(metodo.getCodigo(), new Saldo(metodo.getCodigo(), CERO, 0L));
        for (var movimiento : cajas.sumarMovimientos(caja.getId())) saldos.put(movimiento.getMetodo(), new Saldo(movimiento.getMetodo(), dinero(movimiento.getMonto()), movimiento.getMovimientos()));
        var efectivo = saldos.getOrDefault("EFECTIVO", new Saldo("EFECTIVO", CERO, 0L));
        saldos.put("EFECTIVO", new Saldo("EFECTIVO", efectivo.getEsperado().add(caja.getMontoInicial()), efectivo.getMovimientos() + (caja.getMontoInicial().signum() > 0 ? 1 : 0)));
        return saldos;
    }

    private BigDecimal total(Map<String, Saldo> saldos) {
        return saldos.values().stream().map(Saldo::getEsperado).reduce(CERO, BigDecimal::add);
    }

    private void validarLectura(SesionCaja caja) {
        contexto.validarLocal(caja.getLocalId());
        var usuario = contexto.usuario();
        if (!caja.getUsuarioId().equals(usuario.getId()) && !PermisosPorRol.permisos(usuario.getRol().name()).contains("CAJA_SUPERVISAR"))
            throw new AccessDeniedException("Solo puede consultar su propia caja");
    }

    private SesionCaja actual() {
        var caja = cajas.findByClaveAbierta(contexto.usuario().getId().toString()).orElseThrow(() -> new RecursoNoEncontradoException("No tiene una caja abierta"));
        contexto.validarLocal(caja.getLocalId());
        return caja;
    }

    private SesionCaja obtener(Long id) {
        return cajas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Caja no encontrada"));
    }

    private void exigirAbierta(SesionCaja caja) {
        if (caja.getCierre() != null || caja.getClaveAbierta() == null)
            throw new ReglaNegocioException("La caja está cerrada");
    }

    private BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Saldo {

        private String metodo;

        private BigDecimal esperado;

        private Long movimientos;
    }
}
