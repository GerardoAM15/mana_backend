package pe.edu.upeu.mana.dto.gasto;

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
public class GastoDiarioRequestDTO {

    @NotNull(message = "Fecha obligatoria")
    private LocalDate fecha;

    @NotNull(message = "Monto obligatorio")
    @DecimalMin(value = "0.01", message = "Monto positivo")
    @Digits(integer = 12, fraction = 2, message = "Importe inválido")
    private BigDecimal monto;

    @NotBlank(message = "Descripción obligatoria")
    @Size(max = 500, message = "Máximo 500 caracteres")
    private String descripcion;

    @NotNull(message = "Estado obligatorio")
    private Boolean activo;
}
