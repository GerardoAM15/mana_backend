package pe.edu.upeu.mana.dto.venta;

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
public class VentaRequestDTO {

    @NotBlank(message = "Servicio obligatorio")
    @Size(max = 32, message = "Máximo 32 caracteres")
    private String servicio;

    @NotNull(message = "Indique si es para llevar")
    private Boolean paraLlevar;

    @Positive(message = "Cliente inválido")
    private Long clienteId;

    @NotEmpty(message = "Agregue líneas")
    @Size(max = 100, message = "Máximo 100 líneas")
    private List<@NotNull(message = "Línea obligatoria") @Valid LineaVentaRequestDTO> lineas;

    @Size(max = 50, message = "Máximo 50 pagos")
    private List<@NotNull(message = "Pago obligatorio") @Valid PagoRequestDTO> pagos;
}
