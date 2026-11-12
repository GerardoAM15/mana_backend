package pe.edu.upeu.mana.dto.reporte;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngresoPeriodoResponseDTO {

    private String periodo;

    private BigDecimal contratos;

    private BigDecimal ventas;

    private BigDecimal total;
}
