package pe.edu.upeu.mana.dto.comprobante;
import java.math.BigDecimal;
import pe.edu.upeu.mana.enums.AmbienteFacturacion;
// Lo que el frontend necesita para armar el selector de comprobante. Sin token ni datos sensibles.
public record ComprobanteConfiguracionDTO(boolean habilitada, AmbienteFacturacion ambiente, BigDecimal igvPorcentaje,
    BigDecimal boletaMontoIdentificacion, String rucEmisor, String razonSocialEmisor) {}
