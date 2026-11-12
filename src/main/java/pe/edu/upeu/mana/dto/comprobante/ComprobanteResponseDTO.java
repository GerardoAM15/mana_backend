package pe.edu.upeu.mana.dto.comprobante;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import pe.edu.upeu.mana.enums.*;
public record ComprobanteResponseDTO(Long id, Long localId, Long ventaId, Long contratoId, Long referenciaId, String referenciaNumero,
    TipoComprobante tipo, String tipoSunat, String serie, Integer numero, String numeroCompleto, LocalDateTime fecha, String moneda,
    String rucEmisor, String receptorTipoDoc, String receptorNumDoc, String receptorNombre, String receptorDireccion,
    BigDecimal porcentajeIgv, BigDecimal opGravadas, BigDecimal igv, BigDecimal total, EstadoComprobante estado, AmbienteFacturacion ambiente,
    String sunatCodigo, String sunatMensaje, String hash, Integer intentos, LocalDateTime ultimoIntentoEn,
    String motivoCodigo, String motivoDescripcion, List<ComprobanteDetalleResponseDTO> lineas,
    String receptorEmail, LocalDateTime correoEnviadoEn) {}
