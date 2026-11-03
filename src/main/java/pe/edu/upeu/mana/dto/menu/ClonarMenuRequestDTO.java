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
public class ClonarMenuRequestDTO {

    @NotNull(message = "Fecha origen obligatoria")
    private LocalDate fechaOrigen;

    @NotNull(message = "Fecha destino obligatoria")
    private LocalDate fechaDestino;

    private Set<@NotBlank(message = "Servicio obligatorio") @Size(max = 32, message = "Máximo 32 caracteres") String> servicios;
}
