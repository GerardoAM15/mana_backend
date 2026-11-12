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
public class ConteoCajaRequestDTO {

    @NotBlank(message = "El método es obligatorio")
    @Size(max = 32, message = "Máximo 32 caracteres")
    private String metodo;

    @NotNull(message = "El monto contado es obligatorio")
    @DecimalMin(value = "0", message = "El monto contado no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "El monto admite 12 enteros y 2 decimales")
    private BigDecimal montoContado;
}
