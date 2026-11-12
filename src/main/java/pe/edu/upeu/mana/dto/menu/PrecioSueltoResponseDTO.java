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
public class PrecioSueltoResponseDTO {

    private String servicio;

    private String categoria;

    private BigDecimal precio;

    private BigDecimal recargoLlevar;

    private Boolean activo;
}
