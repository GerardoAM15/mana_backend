package pe.edu.upeu.mana.dto.caja;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCajaResponseDTO {

    private CajaResponseDTO caja;

    private List<MetodoCajaResponseDTO> metodos;

    private BigDecimal montoEsperado;

    private Long ventas;

    private Long consumos;

    private Long contratos;
}
