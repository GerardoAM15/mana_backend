package pe.edu.upeu.mana.dto.menu;

import java.math.BigDecimal;
import java.util.List;
import pe.edu.upeu.mana.enums.PeriodoPlan;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReglaCobroResponseDTO {

    private Long id;

    private String nombre;

    private String servicio;

    private BigDecimal precioCombo;

    private BigDecimal recargoLlevar;

    private Boolean activo;

    private List<ComponenteReglaResponseDTO> componentes;

    private Long version;
}
