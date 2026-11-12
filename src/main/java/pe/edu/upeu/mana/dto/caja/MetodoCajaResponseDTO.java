package pe.edu.upeu.mana.dto.caja;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoCajaResponseDTO {

    private String metodo;

    private BigDecimal montoEsperado;

    private BigDecimal montoContado;

    private BigDecimal diferencia;

    private Long movimientos;
}
