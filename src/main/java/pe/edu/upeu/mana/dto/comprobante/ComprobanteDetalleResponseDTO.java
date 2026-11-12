package pe.edu.upeu.mana.dto.comprobante;
import java.math.BigDecimal;
public record ComprobanteDetalleResponseDTO(Integer item, String codigo, String descripcion, String unidad, BigDecimal cantidad,
    BigDecimal valorUnitario, BigDecimal precioUnitario, BigDecimal valorVenta, BigDecimal igv, BigDecimal total) {}
