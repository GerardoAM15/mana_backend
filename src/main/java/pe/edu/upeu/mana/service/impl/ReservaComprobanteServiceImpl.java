package pe.edu.upeu.mana.service.impl;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.dto.comprobante.ComprobanteRequestDTO;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.security.PermisosPorRol;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.service.service.*;

@Service
public class ReservaComprobanteServiceImpl implements ReservaComprobanteService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReservaComprobanteServiceImpl.class);
    private final ComprobanteRepository comprobantes;
    private final ComprobanteDetalleRepository lineas;
    private final VentaRepository ventas;
    private final ContratoRepository contratos;
    private final DetalleOrdenRepository detallesVenta;
    private final DetalleContratoRepository detallesContrato;
    private final ServicioRepository servicios;
    private final CorrelativoComprobante correlativos;
    private final CalculadoraComprobante calculadora;
    private final ValidadorReceptorComprobante receptores;
    private final ContextoLocalService contexto;
    private final FacturacionProperties config;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;
    public ReservaComprobanteServiceImpl(ComprobanteRepository comprobantes, ComprobanteDetalleRepository lineas, VentaRepository ventas,
            ContratoRepository contratos, DetalleOrdenRepository detallesVenta, DetalleContratoRepository detallesContrato, ServicioRepository servicios,
            CorrelativoComprobante correlativos, CalculadoraComprobante calculadora, ValidadorReceptorComprobante receptores,
            ContextoLocalService contexto, FacturacionProperties config, ApplicationEventPublisher eventos, Clock clock) {
        this.comprobantes=comprobantes; this.lineas=lineas; this.ventas=ventas; this.contratos=contratos; this.detallesVenta=detallesVenta;
        this.detallesContrato=detallesContrato; this.servicios=servicios; this.correlativos=correlativos; this.calculadora=calculadora;
        this.receptores=receptores; this.contexto=contexto; this.config=config; this.eventos=eventos; this.clock=clock;
    }
    private boolean solicitado(ComprobanteRequestDTO r) {
        if (r == null || r.getTipo() == TipoSolicitudComprobante.TICKET || !config.isHabilitada()) return false;
        if (!PermisosPorRol.permisos(contexto.usuario().getRol().name()).contains("COMPROBANTE_EMITIR")) throw new AccessDeniedException("No tiene permiso para emitir comprobantes");
        return true;
    }
    @Transactional
    public Long venta(Long id, ComprobanteRequestDTO r) {
        long inicio=System.nanoTime(); log.debug("Inicio ReservaComprobanteServiceImpl.venta");
        try {
            if (!solicitado(r)) return null;
            var v=ventas.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada"));
            contexto.validarLocal(v.getLocalId());
            if (v.getEstado()!=EstadoVenta.PAGADA) throw new ReglaNegocioException("Solo se emiten comprobantes de ventas pagadas");
            exigirSinVigente(comprobantes.findByVentaIdAndEstadoNot(id, EstadoComprobante.ANULADO), "La venta ya tiene un comprobante vigente");
            var entradas=detallesVenta.findByVentaIdOrderById(id).stream().map(d -> new CalculadoraComprobante.Linea(
                d.getId().toString(), d.getDescripcion(), "NIU", BigDecimal.valueOf(d.getCantidad()), d.getPrecioUnitario().add(d.getRecargoUnitario()))).toList();
            return reservar(v.getLocalId(), id, null, r, v.getTotal(), entradas);
        } finally { log.debug("Fin ReservaComprobanteServiceImpl.venta | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    @Transactional
    public Long contrato(Long id, ComprobanteRequestDTO r) {
        long inicio=System.nanoTime(); log.debug("Inicio ReservaComprobanteServiceImpl.contrato");
        try {
            if (!solicitado(r)) return null;
            var c=contratos.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
            contexto.validarLocal(c.getLocalId());
            if (c.getEstado()==EstadoContrato.ANULADO) throw new ReglaNegocioException("El contrato está anulado");
            exigirSinVigente(comprobantes.findByContratoIdAndEstadoNot(id, EstadoComprobante.ANULADO), "El contrato ya tiene un comprobante vigente");
            var entradas=detallesContrato.findByContratoIdOrderById(id).stream().map(d -> new CalculadoraComprobante.Linea(d.getServicio(),
                "Pensión - " + servicios.findById(d.getServicio()).map(Servicio::getNombre).orElse(d.getServicio()) + " (" + d.getRacionesContratadas() + " raciones)",
                "ZZ", BigDecimal.ONE, d.getSubtotal())).toList();
            return reservar(c.getLocalId(), null, id, r, c.getMontoTotal(), entradas);
        } finally { log.debug("Fin ReservaComprobanteServiceImpl.contrato | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    // Un comprobante RECHAZADO por SUNAT no tiene validez tributaria: se marca ANULADO (sin nota de crédito)
    // para poder emitir uno nuevo, con otro número, sobre la misma venta o contrato. Cualquier otro estado
    // (PENDIENTE, ERROR, ACEPTADO, OBSERVADO) sigue bloqueando una segunda emisión.
    private void exigirSinVigente(Optional<Comprobante> actual, String mensaje) {
        if (actual.isEmpty()) return;
        var c = comprobantes.bloquear(actual.get().getId()).orElseThrow(() -> new RecursoNoEncontradoException("Comprobante no encontrado"));
        if (c.getEstado() != EstadoComprobante.RECHAZADO) throw new ReglaNegocioException(mensaje);
        c.setEstado(EstadoComprobante.ANULADO);
        // Se escribe antes de insertar el nuevo: el índice único de "comprobante vigente" ignora los anulados.
        comprobantes.saveAndFlush(c);
        log.info("Comprobante rechazado reemplazado | id={} | {}-{}", c.getId(), c.getSerie(), c.getNumero());
    }
    private Long reservar(Long local, Long venta, Long contrato, ComprobanteRequestDTO r, BigDecimal total, List<CalculadoraComprobante.Linea> entradas) {
        var receptor=receptores.validar(r,total);
        validarEmisor();
        var ds=calculadora.calcular(entradas,total,config.getIgvPorcentaje());
        var c=new Comprobante();
        c.setTipo(TipoComprobante.valueOf(r.getTipo().name())); c.setLocalId(local); c.setVentaId(venta); c.setContratoId(contrato);
        c.setReceptorTipoDoc(receptor.tipo()); c.setReceptorNumDoc(receptor.numero()); c.setReceptorNombre(receptor.nombre()); c.setReceptorDireccion(receptor.direccion());
        c.setReceptorEmail(r.getReceptorEmail()==null || r.getReceptorEmail().isBlank() ? null : r.getReceptorEmail().trim().toLowerCase());
        c.setPorcentajeIgv(config.getIgvPorcentaje()); c.setTotal(total);
        c.setOpGravadas(ds.stream().map(ComprobanteDetalle::getValorVenta).reduce(BigDecimal.ZERO,BigDecimal::add)); c.setIgv(total.subtract(c.getOpGravadas()));
        guardar(c,ds,TipoSerie.valueOf(c.getTipo().name()));
        return c.getId();
    }
    private void validarEmisor() {
        var e=config.getEmpresa();
        if (!e.getRuc().matches("(10|15|17|20)[0-9]{9}") || e.getRazonSocial().isBlank() || e.getDireccion().isBlank() || !e.getUbigeo().matches("[0-9]{6}"))
            throw new ReglaNegocioException("Complete RUC, razón social, dirección y ubigeo del emisor de facturación");
    }
    private void guardar(Comprobante c, List<ComprobanteDetalle> ds, TipoSerie tipo) {
        var numero=correlativos.siguiente(c.getLocalId(),tipo);
        c.setSerieId(numero.serieId()); c.setSerie(numero.serie()); c.setNumero(numero.numero());
        c.setFechaEmision(LocalDateTime.now(clock)); c.setAmbiente(config.getAmbiente()); c.setEstado(EstadoComprobante.PENDIENTE);
        comprobantes.saveAndFlush(c);
        ds.forEach(d -> { d.setComprobanteId(c.getId()); lineas.save(d); });
        eventos.publishEvent(new ComprobanteReservado(c.getId()));
    }
    @Transactional
    public void anularVenta(Long id) { comprobantes.findByVentaIdAndEstadoNot(id,EstadoComprobante.ANULADO).ifPresent(c -> anular(c.getId())); }
    @Transactional
    public void anularContrato(Long id) { comprobantes.findByContratoIdAndEstadoNot(id,EstadoComprobante.ANULADO).ifPresent(c -> anular(c.getId())); }
    private void anular(Long id) {
        long inicio=System.nanoTime(); log.debug("Inicio ReservaComprobanteServiceImpl.anular");
        try {
            var original=comprobantes.bloquear(id).orElseThrow(() -> new RecursoNoEncontradoException("Comprobante no encontrado"));
            if(original.getEstado()==EstadoComprobante.ANULADO) return;
            if(original.getEstado()==EstadoComprobante.ACEPTADO || original.getEstado()==EstadoComprobante.OBSERVADO) {
                var nota=new Comprobante(); nota.setTipo(TipoComprobante.NOTA_CREDITO); nota.setLocalId(original.getLocalId()); nota.setReferenciaId(id);
                nota.setMotivoCodigo("01"); nota.setMotivoDescripcion("Anulación de la operación");
                nota.setReceptorTipoDoc(original.getReceptorTipoDoc()); nota.setReceptorNumDoc(original.getReceptorNumDoc());
                nota.setReceptorNombre(original.getReceptorNombre()); nota.setReceptorDireccion(original.getReceptorDireccion());
                // Si el cliente recibió la boleta por correo, también recibe la nota de crédito.
                nota.setReceptorEmail(original.getReceptorEmail());
                nota.setPorcentajeIgv(original.getPorcentajeIgv()); nota.setOpGravadas(original.getOpGravadas()); nota.setIgv(original.getIgv()); nota.setTotal(original.getTotal());
                var copia=lineas.findByComprobanteIdOrderByItem(id).stream().map(d -> {
                    var n=new ComprobanteDetalle(); n.setItem(d.getItem()); n.setCodigo(d.getCodigo()); n.setDescripcion(d.getDescripcion()); n.setUnidad(d.getUnidad());
                    n.setCantidad(d.getCantidad()); n.setValorUnitario(d.getValorUnitario()); n.setPrecioUnitario(d.getPrecioUnitario()); n.setValorVenta(d.getValorVenta()); n.setIgv(d.getIgv()); n.setTotal(d.getTotal()); return n;
                }).toList();
                guardar(nota,copia,original.getTipo()==TipoComprobante.BOLETA?TipoSerie.NOTA_CREDITO_BOLETA:TipoSerie.NOTA_CREDITO_FACTURA);
                nota.setAmbiente(original.getAmbiente());
            }
            original.setEstado(EstadoComprobante.ANULADO);
        } finally { log.debug("Fin ReservaComprobanteServiceImpl.anular | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
}
