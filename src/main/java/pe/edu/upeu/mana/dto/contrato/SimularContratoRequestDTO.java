package pe.edu.upeu.mana.dto.contrato;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimularContratoRequestDTO {

    @NotNull(message = "Cliente obligatorio")
    @Positive(message = "Cliente inválido")
    private Long clienteId;

    @NotNull(message = "Plan obligatorio")
    @Positive(message = "Plan inválido")
    private Long planId;

    @NotNull(message = "Fecha de inicio obligatoria")
    private LocalDate fechaInicio;

    @NotEmpty(message = "Seleccione servicios")
    @Size(max = 3, message = "Máximo tres servicios")
    private List<@NotBlank(message = "Servicio obligatorio") @Size(max = 32, message = "Máximo 32 caracteres") String> servicios;
}
