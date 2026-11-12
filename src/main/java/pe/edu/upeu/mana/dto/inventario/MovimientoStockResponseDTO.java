package pe.edu.upeu.mana.dto.inventario;

import java.time.LocalDateTime;
import pe.edu.upeu.mana.enums.TipoMovimientoStock;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoStockResponseDTO {

    private Long id;

    private Long localId;

    private Long bebidaId;

    private String bebidaNombre;

    private TipoMovimientoStock tipo;

    private Long cantidad;

    private LocalDateTime fecha;

    private Long detalleCompraId;

    private Long detalleOrdenId;

    private String motivo;
}
