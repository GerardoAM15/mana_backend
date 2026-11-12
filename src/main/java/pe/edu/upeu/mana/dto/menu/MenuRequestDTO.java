package pe.edu.upeu.mana.dto.menu;

import java.time.LocalDate;
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
public class MenuRequestDTO {

    @NotNull(message = "Fecha obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "Servicio obligatorio")
    @Size(max = 32, message = "Máximo 32 caracteres")
    private String servicio;

    @NotEmpty(message = "Seleccione platos")
    private Set<@NotNull(message = "Plato obligatorio") @Positive(message = "Plato inválido") Long> platoIds;
}
