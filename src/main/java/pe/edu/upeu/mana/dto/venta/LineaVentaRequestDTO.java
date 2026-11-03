package pe.edu.upeu.mana.dto.venta;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.edu.upeu.mana.enums.TipoLineaVenta;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pe.edu.upeu.mana.dto.consumo.PlatoCantidadRequestDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineaVentaRequestDTO {

    @NotNull(message = "Tipo obligatorio")
    private TipoLineaVenta tipo;

    @NotNull(message = "Cantidad obligatoria")
    @Min(value = 1, message = "Cantidad positiva")
    @Max(value = 9999999999L, message = "Cantidad fuera de rango")
    private Long cantidad;

    @Positive(message = "Plato inválido")
    private Long platoId;

    @Positive(message = "Regla inválida")
    private Long reglaId;

    @Positive(message = "Bebida inválida")
    private Long bebidaId;

    @Size(max = 50, message = "Máximo 50 platos")
    private List<@NotNull(message = "Plato obligatorio") @Valid PlatoCantidadRequestDTO> platos;

    @AssertTrue(message = "Indique solo el identificador del tipo elegido; COMBO requiere sus platos")
    public boolean isFormaValida() {
        if (tipo == null)
            return false;
        return switch(tipo) {
            case PLATO ->
                platoId != null && reglaId == null && bebidaId == null && (platos == null || platos.isEmpty());
            case BEBIDA ->
                bebidaId != null && platoId == null && reglaId == null && (platos == null || platos.isEmpty());
            case COMBO ->
                reglaId != null && platoId == null && bebidaId == null && platos != null && !platos.isEmpty();
        };
    }
}
