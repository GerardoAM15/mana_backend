package pe.edu.upeu.mana.dto.usuario;

import jakarta.validation.constraints.NotNull;
import pe.edu.upeu.mana.enums.RolUsuario;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolRequestDTO {

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;
}
