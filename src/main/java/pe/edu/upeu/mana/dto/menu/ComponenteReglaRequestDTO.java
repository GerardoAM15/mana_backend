package pe.edu.upeu.mana.dto.menu;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteReglaRequestDTO {

    @NotBlank(message = "Categoría obligatoria")
    @Size(max = 32, message = "Máximo 32 caracteres")
    private String categoria;

    @NotNull(message = "Cantidad obligatoria")
    @Min(value = 1, message = "Cantidad positiva")
    @Max(value = 9999999999L, message = "Cantidad fuera de rango")
    private Long cantidad;
}
