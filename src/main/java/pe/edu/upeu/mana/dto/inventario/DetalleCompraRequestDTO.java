package pe.edu.upeu.mana.dto.inventario;

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
public class DetalleCompraRequestDTO {

    @NotNull(message = "Bebida obligatoria")
    @Positive(message = "Bebida inválida")
    private Long bebidaId;

    @NotNull(message = "Presentaciones obligatorias")
    @Min(value = 1, message = "Presentaciones positivas")
    @Max(value = 9999999999L, message = "Cantidad fuera de rango")
    private Long presentaciones;

    @NotNull(message = "Unidades obligatorias")
    @Min(value = 1, message = "Unidades positivas")
    @Max(value = 9999999999L, message = "Cantidad fuera de rango")
    private Long unidadesPorPresentacion;

    @NotNull(message = "Costo obligatorio")
    @DecimalMin(value = "0.01", message = "Costo positivo")
    @Digits(integer = 12, fraction = 2, message = "Importe inválido")
    private BigDecimal costoTotal;
}
