package pe.edu.upeu.mana.dto.menu;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CopiarCenaRequestDTO {

    @NotNull(message = "Fecha obligatoria")
    private LocalDate fecha;
}
