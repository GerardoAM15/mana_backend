package pe.edu.upeu.mana.dto.local;

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
public class LocalRequestDTO {

    @NotBlank(message = "El campo nombre es obligatorio")
    @Size(max = 120, message = "El campo nombre admite hasta 120 caracteres")
    private String nombre;

    @Size(max = 250, message = "El campo direccion admite hasta 250 caracteres")
    private String direccion;

    @Size(max = 20, message = "El campo telefono admite hasta 20 caracteres")
    private String telefono;

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}
