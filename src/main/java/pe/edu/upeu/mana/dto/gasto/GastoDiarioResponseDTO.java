package pe.edu.upeu.mana.dto.gasto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GastoDiarioResponseDTO {

    private Long id;

    private Long localId;

    private LocalDate fecha;

    private BigDecimal monto;

    private String descripcion;

    private Boolean activo;

    private Long version;
}
