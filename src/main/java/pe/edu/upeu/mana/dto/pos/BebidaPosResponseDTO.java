package pe.edu.upeu.mana.dto.pos;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BebidaPosResponseDTO {

    private Long bebidaId;

    private String nombre;

    private BigDecimal precioVenta;

    private Long stock;
}
