package pe.edu.upeu.mana.dto.menu;

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
public class PrecioSueltoRequestDTO {

    @NotBlank(message = "Servicio obligatorio")
    @Size(max = 32, message = "Máximo 32 caracteres")
    private String servicio;

    @NotBlank(message = "Categoría obligatoria")
    @Size(max = 32, message = "Máximo 32 caracteres")
    private String categoria;

    @NotNull(message = "Precio obligatorio")
    @DecimalMin(value = "0.01", message = "Precio positivo")
    @Digits(integer = 12, fraction = 2, message = "Importe inválido")
    private BigDecimal precio;

    @NotNull(message = "Recargo obligatorio")
    @DecimalMin(value = "0", message = "Recargo no negativo")
    @Digits(integer = 12, fraction = 2, message = "Importe inválido")
    private BigDecimal recargoLlevar;

    @NotNull(message = "Estado obligatorio")
    private Boolean activo;
}
