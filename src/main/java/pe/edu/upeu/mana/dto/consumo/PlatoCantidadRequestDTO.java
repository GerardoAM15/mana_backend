package pe.edu.upeu.mana.dto.consumo;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatoCantidadRequestDTO {

    @NotNull(message = "Plato obligatorio")
    @Positive(message = "Plato inválido")
    private Long platoId;

    @NotNull(message = "Cantidad obligatoria")
    @Min(value = 1, message = "Cantidad positiva")
    @Max(value = 9999999999L, message = "Cantidad fuera de rango")
    private Long cantidad;
}
