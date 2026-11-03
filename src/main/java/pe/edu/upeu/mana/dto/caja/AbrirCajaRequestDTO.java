package pe.edu.upeu.mana.dto.caja;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbrirCajaRequestDTO {

    @NotNull(message = "El monto inicial es obligatorio")
    @DecimalMin(value = "0", message = "El monto inicial no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "El monto admite 12 enteros y 2 decimales")
    private BigDecimal montoInicial;
}
