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
public class PagosVentaRequestDTO {

    @NotEmpty(message = "Agregue pagos")
    @Size(max = 50, message = "Máximo 50 pagos")
    private List<@NotNull(message = "Pago obligatorio") @Valid PagoRequestDTO> pagos;
    @Valid
    private pe.edu.upeu.mana.dto.comprobante.ComprobanteRequestDTO comprobante;

    public PagosVentaRequestDTO(List<PagoRequestDTO> pagos) {
        this(pagos, null);
    }
}
