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
public class GastoIngresoResponseDTO {

    private String periodo;

    private BigDecimal ingresos;

    private BigDecimal gastos;

    private BigDecimal diferencia;
}
