package pe.edu.upeu.mana.dto.venta;

import java.math.BigDecimal;
import java.util.List;
import pe.edu.upeu.mana.enums.TipoLineaVenta;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineaVentaResponseDTO {

    private Long id;

    private TipoLineaVenta tipo;

    private Long platoId;

    private Long reglaId;

    private Long bebidaId;

    private String descripcion;

    private Long cantidad;

    private BigDecimal precioUnitario;

    private BigDecimal recargoUnitario;

    private List<PlatoComboResponseDTO> platos;
}
