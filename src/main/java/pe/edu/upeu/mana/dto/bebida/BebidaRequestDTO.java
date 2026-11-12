package pe.edu.upeu.mana.dto.bebida;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BebidaRequestDTO {

    @NotBlank(message = "Nombre obligatorio")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String nombre;

    @NotNull(message = "Precio obligatorio")
    @DecimalMin(value = "0.01", message = "Precio positivo")
    @Digits(integer = 12, fraction = 2, message = "Importe inválido")
    private BigDecimal precioVenta;

    @NotNull(message = "Estado obligatorio")
    private Boolean activo;
}
