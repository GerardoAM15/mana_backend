package pe.edu.upeu.mana.dto.contrato;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.edu.upeu.mana.enums.TipoPagoContrato;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoContratoResponseDTO {

    private Long id;

    private Long cajaId;

    private TipoPagoContrato tipo;

    private String metodo;

    private BigDecimal monto;

    private LocalDateTime fecha;

    private String referencia;
}
