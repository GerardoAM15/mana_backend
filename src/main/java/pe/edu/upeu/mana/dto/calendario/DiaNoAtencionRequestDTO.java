package pe.edu.upeu.mana.dto.calendario;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaNoAtencionRequestDTO {

    @NotNull(message = "Fecha obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "Motivo obligatorio")
    @Size(max = 160, message = "Máximo 160 caracteres")
    private String motivo;
}
