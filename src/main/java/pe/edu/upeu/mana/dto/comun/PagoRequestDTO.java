package pe.edu.upeu.mana.dto.comun;

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
public class PagoRequestDTO {

    @NotBlank(message = "El método es obligatorio")
    @Size(max = 32, message = "Máximo 32 caracteres")
    private String metodo;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser positivo")
    @Digits(integer = 12, fraction = 2, message = "El importe admite 12 enteros y 2 decimales")
    private BigDecimal monto;

    @Size(max = 160, message = "Máximo 160 caracteres")
    private String referencia;
}
