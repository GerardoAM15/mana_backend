package pe.edu.upeu.mana.dto.contrato;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pe.edu.upeu.mana.dto.comun.PagoRequestDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenovarContratoRequestDTO {

    @NotNull(message = "Plan obligatorio")
    @Positive(message = "Plan inválido")
    private Long planId;

    @NotEmpty(message = "Seleccione servicios")
    @Size(max = 3, message = "Máximo tres servicios")
    private List<@NotBlank(message = "Servicio obligatorio") @Size(max = 32, message = "Máximo 32 caracteres") String> servicios;

    @NotEmpty(message = "Los pagos son obligatorios")
    @Size(max = 50, message = "Máximo 50 pagos")
    private List<@NotNull(message = "Pago obligatorio") @Valid PagoRequestDTO> pagos;
}
