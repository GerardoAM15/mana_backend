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
public class PlatoRequestDTO {

    @NotBlank(message = "El campo nombre es obligatorio")
    @Size(max = 120, message = "El campo nombre admite hasta 120 caracteres")
    private String nombre;

    @NotBlank(message = "El campo categoria es obligatorio")
    @Size(max = 32, message = "El campo categoria admite hasta 32 caracteres")
    private String categoria;

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}
