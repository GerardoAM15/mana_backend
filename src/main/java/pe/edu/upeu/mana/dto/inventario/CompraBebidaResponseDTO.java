package pe.edu.upeu.mana.dto.inventario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompraBebidaResponseDTO {

    private Long id;

    private Long localId;

    private Long proveedorId;

    private String proveedorNombre;

    private LocalDate fecha;

    private BigDecimal total;

    private List<DetalleCompraResponseDTO> detalles;
}
