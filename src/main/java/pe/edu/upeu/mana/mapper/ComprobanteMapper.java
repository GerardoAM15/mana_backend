package pe.edu.upeu.mana.mapper;
import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.comprobante.*;
import pe.edu.upeu.mana.config.FacturacionProperties;
@Component
public class ComprobanteMapper {
    private final FacturacionProperties config;
    public ComprobanteMapper(FacturacionProperties config) { this.config=config; }
    public ComprobanteResponseDTO toResponse(Comprobante c,List<ComprobanteDetalle> ds,Comprobante ref) {
        return new ComprobanteResponseDTO(c.getId(),c.getLocalId(),c.getVentaId(),c.getContratoId(),c.getReferenciaId(),ref==null?null:numero(ref),
            c.getTipo(),ComprobantePayloadMapper.tipoSunat(c.getTipo()),c.getSerie(),c.getNumero(),numero(c),c.getFechaEmision(),c.getMoneda(),
            config.getEmpresa().getRuc(),c.getReceptorTipoDoc(),c.getReceptorNumDoc(),c.getReceptorNombre(),c.getReceptorDireccion(),
            c.getPorcentajeIgv(),c.getOpGravadas(),c.getIgv(),c.getTotal(),c.getEstado(),c.getAmbiente(),c.getSunatCodigo(),c.getSunatMensaje(),c.getHash(),
            c.getIntentos(),c.getUltimoIntentoEn(),c.getMotivoCodigo(),c.getMotivoDescripcion(),ds.stream().map(d -> new ComprobanteDetalleResponseDTO(
                d.getItem(),d.getCodigo(),d.getDescripcion(),d.getUnidad(),d.getCantidad(),d.getValorUnitario(),d.getPrecioUnitario(),d.getValorVenta(),d.getIgv(),d.getTotal())).toList(),
            c.getReceptorEmail(),c.getCorreoEnviadoEn());
    }
    public static String numero(Comprobante c) { return "%s-%08d".formatted(c.getSerie(),c.getNumero()); }
}
