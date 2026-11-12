package pe.edu.upeu.mana.dto.pos;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatoSueltoPosResponseDTO {

    private Long platoId;

    private String nombre;

    private String categoria;

    private BigDecimal precio;

    private BigDecimal recargoLlevar;
}
