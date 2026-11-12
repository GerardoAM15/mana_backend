package pe.edu.upeu.mana.dto.menu;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatoResponseDTO {

    private Long id;

    private String nombre;

    private String categoria;

    private Boolean activo;

    private Long version;
}
