package pe.edu.upeu.mana.mapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.TipoComprobante;

@Component
public class ComprobantePayloadMapper {
    private final FacturacionProperties config;
    private final Clock clock;
    public ComprobantePayloadMapper(FacturacionProperties config, Clock clock) { this.config=config; this.clock=clock; }
    // APIsPERU exige exactamente yyyy-MM-ddTHH:mm:ss-05:00: sin fracción de segundo (LocalDateTime.now trae
    // nanosegundos y OffsetDateTime.toString omite los segundos en cero; ambos dan 400).
    private static final DateTimeFormatter FECHA_SUNAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    String fechaSunat(LocalDateTime fecha) { return fecha.truncatedTo(ChronoUnit.SECONDS).atZone(clock.getZone()).format(FECHA_SUNAT); }
    public static String tipoSunat(TipoComprobante tipo) { return switch(tipo) { case BOLETA -> "03"; case FACTURA -> "01"; case NOTA_CREDITO -> "07"; }; }
    public Map<String,Object> toPayload(Comprobante c, List<ComprobanteDetalle> lineas, Comprobante referencia) {
        var p=new LinkedHashMap<String,Object>();
        p.put("ublVersion","2.1"); p.put("tipoDoc",tipoSunat(c.getTipo())); p.put("serie",c.getSerie()); p.put("correlativo",c.getNumero().toString());
        p.put("fechaEmision",fechaSunat(c.getFechaEmision())); p.put("tipoMoneda",c.getMoneda());
        if(c.getTipo()!=TipoComprobante.NOTA_CREDITO) { p.put("tipoOperacion","0101"); p.put("formaPago",Map.of("moneda","PEN","tipo","Contado")); }
        else { p.put("codMotivo",c.getMotivoCodigo()); p.put("desMotivo",c.getMotivoDescripcion());
            p.put("tipDocAfectado",tipoSunat(referencia.getTipo())); p.put("numDocfectado",referencia.getSerie()+"-"+referencia.getNumero()); }
        var client=new LinkedHashMap<String,Object>(); client.put("tipoDoc",c.getReceptorTipoDoc());
        client.put("numDoc",c.getReceptorNumDoc()==null?"-":c.getReceptorNumDoc()); client.put("rznSocial",c.getReceptorNombre());
        if(c.getReceptorDireccion()!=null) client.put("address",Map.of("direccion",c.getReceptorDireccion()));
        p.put("client",client);
        var e=config.getEmpresa();
        var direccion=new LinkedHashMap<String,Object>(); direccion.put("direccion",e.getDireccion()); direccion.put("ubigueo",e.getUbigeo());
        if(!e.getDepartamento().isBlank()) direccion.put("departamento",e.getDepartamento());
        if(!e.getProvincia().isBlank()) direccion.put("provincia",e.getProvincia());
        if(!e.getDistrito().isBlank()) direccion.put("distrito",e.getDistrito());
        if(!e.getCodLocal().isBlank()) direccion.put("codLocal",e.getCodLocal());
        p.put("company",Map.of("ruc",e.getRuc(),"razonSocial",e.getRazonSocial(),"nombreComercial",e.getNombreComercial(),"address",direccion));
        p.put("mtoOperGravadas",c.getOpGravadas()); p.put("mtoIGV",c.getIgv()); p.put("totalImpuestos",c.getIgv());
        p.put("valorVenta",c.getOpGravadas()); p.put("subTotal",c.getTotal()); p.put("mtoImpVenta",c.getTotal());
        p.put("details",lineas.stream().map(d -> {
            var l=new LinkedHashMap<String,Object>(); if(d.getCodigo()!=null) l.put("codProducto",d.getCodigo());
            l.put("unidad",d.getUnidad()); l.put("descripcion",d.getDescripcion()); l.put("cantidad",d.getCantidad()); l.put("mtoValorUnitario",d.getValorUnitario());
            l.put("mtoValorVenta",d.getValorVenta()); l.put("mtoBaseIgv",d.getValorVenta()); l.put("porcentajeIgv",c.getPorcentajeIgv());
            l.put("igv",d.getIgv()); l.put("tipAfeIgv","10"); l.put("totalImpuestos",d.getIgv()); l.put("mtoPrecioUnitario",d.getPrecioUnitario()); return l;
        }).toList());
        return p;
    }
}
