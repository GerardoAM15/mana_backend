package pe.edu.upeu.mana.dto.proveedor;

import java.util.Set;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorRequestDTO {

    @NotBlank(message = "Razón social obligatoria")
    @Size(max = 160, message = "Máximo 160 caracteres")
    private String razonSocial;

    @Size(max = 20, message = "Máximo 20 caracteres")
    private String telefono;

    @NotNull(message = "Estado obligatorio")
    private Boolean activo;

    @NotNull(message = "Indique las bebidas")
    private Set<@NotNull(message = "Bebida obligatoria") @Positive(message = "Bebida inválida") Long> bebidaIds;
}
