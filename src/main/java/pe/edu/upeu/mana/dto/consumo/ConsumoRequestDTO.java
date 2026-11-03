package pe.edu.upeu.mana.dto.consumo;

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
public class ConsumoRequestDTO {

    @NotNull(message = "Contrato obligatorio")
    @Positive(message = "Contrato inválido")
    private Long contratoId;

    @NotBlank(message = "Servicio obligatorio")
    @Size(max = 32, message = "Máximo 32 caracteres")
    private String servicio;

    @NotEmpty(message = "Seleccione platos")
    @Size(max = 50, message = "Máximo 50 platos")
    private List<@NotNull(message = "Plato obligatorio") @Valid PlatoCantidadRequestDTO> platos;

    @NotNull(message = "Indique si es para llevar")
    private Boolean paraLlevar;

    @Size(max = 32, message = "Máximo 32 caracteres")
    private String metodoRecargo;
}
