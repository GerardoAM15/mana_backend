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
public class TarifaServicioRequestDTO {

    @NotBlank(message = "El campo servicio es obligatorio")
    @Size(max = 32, message = "El campo servicio admite hasta 32 caracteres")
    private String servicio;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe debe ser positivo")
    @Digits(integer = 12, fraction = 2, message = "El importe admite 12 enteros y 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}
