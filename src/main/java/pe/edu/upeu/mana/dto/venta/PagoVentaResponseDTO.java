package pe.edu.upeu.mana.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoVentaResponseDTO {

    private Long id;

    private String metodo;

    private BigDecimal monto;

    private LocalDateTime fecha;
}
