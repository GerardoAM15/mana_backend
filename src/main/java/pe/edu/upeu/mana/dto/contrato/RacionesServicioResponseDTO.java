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
public class RacionesServicioResponseDTO {

    private String servicio;

    private long raciones;

    private BigDecimal precioRacion;

    private BigDecimal subtotal;
}
