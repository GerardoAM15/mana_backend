package pe.edu.upeu.mana.dto.contrato;

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
public class SimulacionContratoResponseDTO {

    private Long clienteId;

    private Long planId;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private List<RacionesServicioResponseDTO> servicios;

    private BigDecimal total;
}
