package pe.edu.upeu.mana.dto.bebida;

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
public class BebidaResponseDTO {

    private Long id;

    private String nombre;

    private BigDecimal precioVenta;

    private Boolean activo;

    private Long version;
}
