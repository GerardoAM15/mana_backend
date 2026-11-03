package pe.edu.upeu.mana.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestablecerClaveRequestDTO {

    @NotBlank(message = "El token es obligatorio")
    @Size(max = 100, message = "Token inválido")
    private String token;

    @NotBlank(message = "La nueva clave es obligatoria")
    @Size(min = 8, max = 72, message = "La clave debe tener entre 8 y 72 caracteres")
    private String nuevaClave;
}
