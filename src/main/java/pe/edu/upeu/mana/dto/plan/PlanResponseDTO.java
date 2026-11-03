package pe.edu.upeu.mana.dto.plan;

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
public class PlanResponseDTO {

    private Long id;

    private String nombre;

    private PeriodoPlan periodo;

    private Boolean activo;

    private List<String> servicios;

    private Long version;
}
