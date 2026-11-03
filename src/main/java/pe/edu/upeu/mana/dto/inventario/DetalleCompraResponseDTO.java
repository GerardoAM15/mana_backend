package pe.edu.upeu.mana.dto.inventario;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCompraResponseDTO {

    private Long id;

    private Long bebidaId;

    private String bebidaNombre;

    private Long presentaciones;

    private Long unidadesPorPresentacion;

    private Long unidades;

    private BigDecimal costoTotal;
}
