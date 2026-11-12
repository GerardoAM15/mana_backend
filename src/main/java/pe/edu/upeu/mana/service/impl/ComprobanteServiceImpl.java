package pe.edu.upeu.mana.service.impl;

import java.time.LocalDate;
import java.util.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.dto.comprobante.*;
import pe.edu.upeu.mana.entity.Comprobante;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.security.PermisosPorRol;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.util.PaginacionUtil;

@Service
public class ComprobanteServiceImpl implements ComprobanteService {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(ComprobanteServiceImpl.class);
    private final ComprobanteRepository comprobantes;
    private final ComprobanteDetalleRepository detalles;
    private final ReservaComprobanteService reserva;
    private final ContextoLocalService contexto;
    private final ComprobanteMapper mapper;
    private final ComprobantePayloadMapper payload;
    private final ApisPeruFacturacionCliente cliente;
    private final EnvioComprobanteService envio;
    private final VerificadorAmbienteFacturacion ambientes;
    private final FacturacionProperties config;
    private final CorreoComprobante correos;
    public ComprobanteServiceImpl(ComprobanteRepository comprobantes,ComprobanteDetalleRepository detalles,ReservaComprobanteService reserva,
            ContextoLocalService contexto,ComprobanteMapper mapper,ComprobantePayloadMapper payload,ApisPeruFacturacionCliente cliente,
            EnvioComprobanteService envio,VerificadorAmbienteFacturacion ambientes,FacturacionProperties config,CorreoComprobante correos) {
        this.comprobantes=comprobantes; this.detalles=detalles; this.reserva=reserva; this.contexto=contexto; this.mapper=mapper;
        this.payload=payload; this.cliente=cliente; this.envio=envio; this.ambientes=ambientes; this.config=config; this.correos=correos;
    }
    // Envío manual del PDF y XML al correo indicado (o al guardado). Solo comprobantes aceptados.
    @Transactional
    public ComprobanteResponseDTO enviarCorreo(Long id,String email) {
        habilitada(); var c=comprobantes.bloquear(id).orElseThrow(() -> new RecursoNoEncontradoException("Comprobante no encontrado"));
        contexto.validarLocal(c.getLocalId());
        return respuesta(correos.enviarAhora(c,email));
    }
    @Transactional(readOnly=true)
    public PaginaResponseDTO<ComprobanteResponseDTO> listar(LocalDate desde,LocalDate hasta,TipoComprobante tipo,EstadoComprobante estado,Long localId,int pagina,int tamanio) {
        long inicio=System.nanoTime(); log.debug("Inicio ComprobanteServiceImpl.listar");
        try {
        if(desde!=null && hasta!=null && desde.isAfter(hasta)) throw new DatosInvalidosException("Rango de fechas inválido");
        var page=PaginacionUtil.construir(pagina,tamanio,"id","desc",Set.of("id"),"id");
        Long local=localId==null && PermisosPorRol.permisos(contexto.usuario().getRol().name()).contains("COMPROBANTE_GESTIONAR")?null:contexto.localId(localId);
        Specification<Comprobante> filtro=(root,q,cb) -> {
            var ps=new ArrayList<Predicate>();
            if(local!=null) ps.add(cb.equal(root.get("localId"),local));
            if(tipo!=null) ps.add(cb.equal(root.get("tipo"),tipo)); if(estado!=null) ps.add(cb.equal(root.get("estado"),estado));
            if(desde!=null) ps.add(cb.greaterThanOrEqualTo(root.get("fechaEmision"),desde.atStartOfDay()));
            if(hasta!=null) ps.add(cb.lessThan(root.get("fechaEmision"),hasta.plusDays(1).atStartOfDay()));
            return cb.and(ps.toArray(Predicate[]::new));
        };
        return PaginaResponseDTO.de(comprobantes.findAll(filtro,page).map(this::respuesta));

        } finally { log.debug("Fin ComprobanteServiceImpl.listar | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    @Transactional(readOnly=true)
    public ComprobanteConfiguracionDTO configuracion() {
        var e=config.getEmpresa();
        return new ComprobanteConfiguracionDTO(config.isHabilitada(),config.getAmbiente(),config.getIgvPorcentaje(),
            config.getBoletaMontoIdentificacion(),e.getRuc(),e.getRazonSocial());
    }
    public ComprobanteResponseDTO buscar(Long id) {
        long inicio=System.nanoTime(); log.debug("Inicio ComprobanteServiceImpl.buscar");
        try { return respuesta(obtener(id));
        } finally { log.debug("Fin ComprobanteServiceImpl.buscar | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    @Transactional(readOnly=true)
    public ComprobanteResponseDTO venta(Long ventaId) {
        long inicio=System.nanoTime(); log.debug("Inicio ComprobanteServiceImpl.venta");
        try {
        var c=comprobantes.findByVentaIdAndEstadoNot(ventaId,EstadoComprobante.ANULADO).orElseThrow(() -> new RecursoNoEncontradoException("La venta no tiene comprobante vigente"));
        contexto.validarLocal(c.getLocalId()); return respuesta(c);

        } finally { log.debug("Fin ComprobanteServiceImpl.venta | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    @Transactional(readOnly=true)
    public ComprobanteResponseDTO contrato(Long contratoId) {
        long inicio=System.nanoTime(); log.debug("Inicio ComprobanteServiceImpl.contrato");
        try {
        var c=comprobantes.findByContratoIdAndEstadoNot(contratoId,EstadoComprobante.ANULADO).orElseThrow(() -> new RecursoNoEncontradoException("El contrato no tiene comprobante vigente"));
        contexto.validarLocal(c.getLocalId()); return respuesta(c);

        } finally { log.debug("Fin ComprobanteServiceImpl.contrato | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    // Sin transacción exterior: la reserva se confirma antes de informar un 409 de ambiente.
    public ComprobanteResponseDTO emitirVenta(Long id,ComprobanteRequestDTO r) {
        long inicio=System.nanoTime(); log.debug("Inicio ComprobanteServiceImpl.emitirVenta");
        try {
        habilitada(); validarSolicitud(r); Long creado=reserva.venta(id,r); validarAmbiente(); return buscar(creado);

        } finally { log.debug("Fin ComprobanteServiceImpl.emitirVenta | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    public ComprobanteResponseDTO emitirContrato(Long id,ComprobanteRequestDTO r) {
        long inicio=System.nanoTime(); log.debug("Inicio ComprobanteServiceImpl.emitirContrato");
        try {
        habilitada(); validarSolicitud(r); Long creado=reserva.contrato(id,r); validarAmbiente(); return buscar(creado);

        } finally { log.debug("Fin ComprobanteServiceImpl.emitirContrato | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    public ComprobanteResponseDTO reenviar(Long id) {
        long inicio=System.nanoTime(); log.debug("Inicio ComprobanteServiceImpl.reenviar");
        try {
        habilitada(); var c=obtener(id);
        if(c.getEstado()!=EstadoComprobante.PENDIENTE && c.getEstado()!=EstadoComprobante.ERROR) throw new ReglaNegocioException("Solo se reenvían comprobantes pendientes o con error");
        if(c.getIntentos()>=5) throw new ReglaNegocioException("El comprobante alcanzó el máximo de cinco intentos");
        envio.enviar(id); correos.enviarSiCorresponde(id); validarAmbiente(); return buscar(id);

        } finally { log.debug("Fin ComprobanteServiceImpl.reenviar | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    @Transactional(readOnly=true)
    public byte[] pdf(Long id) {
        long inicio=System.nanoTime(); log.debug("Inicio ComprobanteServiceImpl.pdf");
        try {
        habilitada(); var c=obtener(id); var ref=c.getReferenciaId()==null?null:comprobantes.findById(c.getReferenciaId()).orElse(null);
        var bytes=cliente.pdf(c.getTipo()==TipoComprobante.NOTA_CREDITO,payload.toPayload(c,detalles.findByComprobanteIdOrderByItem(id),ref));
        if(bytes==null || bytes.length==0) throw new ServicioExternoException("La API no devolvió el PDF del comprobante"); return bytes;

        } finally { log.debug("Fin ComprobanteServiceImpl.pdf | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    @Transactional(readOnly=true)
    public String xml(Long id) {
        long inicio=System.nanoTime(); log.debug("Inicio ComprobanteServiceImpl.xml");
        try {
        var c=obtener(id); if(c.getXml()==null || c.getXml().isBlank()) throw new RecursoNoEncontradoException("El comprobante todavía no tiene XML firmado"); return c.getXml();

        } finally { log.debug("Fin ComprobanteServiceImpl.xml | duracionMs={}",(System.nanoTime()-inicio)/1_000_000); }
    }
    private Comprobante obtener(Long id) {
        var c=comprobantes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Comprobante no encontrado")); contexto.validarLocal(c.getLocalId()); return c;
    }
    private ComprobanteResponseDTO respuesta(Comprobante c) {
        return mapper.toResponse(c,detalles.findByComprobanteIdOrderByItem(c.getId()),c.getReferenciaId()==null?null:comprobantes.findById(c.getReferenciaId()).orElse(null));
    }
    private void habilitada() { if(!config.isHabilitada()) throw new ReglaNegocioException("La facturación electrónica no está habilitada"); }
    private void validarSolicitud(ComprobanteRequestDTO r) {
        if(r==null || r.getTipo()==null || r.getTipo()==TipoSolicitudComprobante.TICKET) throw new DatosInvalidosException("Seleccione boleta o factura");
    }
    private void validarAmbiente() {
        var v=ambientes.verificar(); if(v.estado()==VerificadorAmbienteFacturacion.Estado.DISCREPANCIA) throw new ReglaNegocioException(v.mensaje());
    }
}
