package pe.edu.upeu.mana.dto.contrato;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleContratoResponseDTO {

    private Long id;

    private String servicio;

    private Long racionesContratadas;

    private Long saldoRaciones;

    private BigDecimal precioRacionPactado;

    private BigDecimal subtotal;
}
