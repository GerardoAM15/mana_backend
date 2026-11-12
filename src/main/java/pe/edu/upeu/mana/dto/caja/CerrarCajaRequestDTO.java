package pe.edu.upeu.mana.dto.caja;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CerrarCajaRequestDTO {

    @NotNull(message = "Los conteos son obligatorios")
    @Size(max = 50, message = "Máximo 50 métodos")
    private List<@NotNull(message = "El conteo no puede ser nulo") @Valid ConteoCajaRequestDTO> conteos;

    @Size(max = 250, message = "Máximo 250 caracteres")
    private String observacion;
}
