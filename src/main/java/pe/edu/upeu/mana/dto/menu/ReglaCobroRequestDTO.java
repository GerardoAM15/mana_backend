package pe.edu.upeu.mana.dto.menu;

import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.edu.upeu.mana.enums.PeriodoPlan;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReglaCobroRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String nombre;

    @NotBlank(message = "El servicio es obligatorio")
    @Size(max = 32, message = "Máximo 32 caracteres")
    private String servicio;

    @NotNull(message = "Precio obligatorio")
    @DecimalMin(value = "0.01", message = "Precio positivo")
    @Digits(integer = 12, fraction = 2, message = "Importe inválido")
    private BigDecimal precioCombo;

    @NotNull(message = "Recargo obligatorio")
    @DecimalMin(value = "0", message = "Recargo no negativo")
    @Digits(integer = 12, fraction = 2, message = "Importe inválido")
    private BigDecimal recargoLlevar;

    @NotNull(message = "Estado obligatorio")
    private Boolean activo;

    @NotEmpty(message = "La composición es obligatoria")
    private List<@NotNull(message = "Componente obligatorio") @Valid ComponenteReglaRequestDTO> componentes;
}
