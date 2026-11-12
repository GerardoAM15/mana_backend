package pe.edu.upeu.mana.dto.comun;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnularRequestDTO {

    @NotBlank(message = "El motivo es obligatorio")
    @Size(min = 10, max = 250, message = "El motivo debe tener entre 10 y 250 caracteres")
    private String motivo;
}
