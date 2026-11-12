package pe.edu.upeu.mana.dto.cliente;

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
public class ClienteRequestDTO {

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "[0-9]{8}", message = "El DNI debe tener 8 dígitos")
    private String dni;

    @NotBlank(message = "El campo nombre es obligatorio")
    @Size(max = 100, message = "El campo nombre admite hasta 100 caracteres")
    private String nombre;

    @NotBlank(message = "El campo apellido es obligatorio")
    @Size(max = 100, message = "El campo apellido admite hasta 100 caracteres")
    private String apellido;

    @Size(max = 20, message = "El campo telefono admite hasta 20 caracteres")
    private String telefono;

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}
