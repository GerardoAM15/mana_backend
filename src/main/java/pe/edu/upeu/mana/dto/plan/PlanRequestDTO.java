package pe.edu.upeu.mana.dto.plan;

import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.edu.upeu.mana.enums.PeriodoPlan;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String nombre;

    @NotNull(message = "El periodo es obligatorio")
    private PeriodoPlan periodo;

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;

    @NotEmpty(message = "Seleccione al menos un servicio")
    private Set<@NotBlank(message = "Servicio obligatorio") String> servicios;
}
