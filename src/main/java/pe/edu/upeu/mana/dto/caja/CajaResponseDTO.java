package pe.edu.upeu.mana.dto.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CajaResponseDTO {

    private Long id;

    private Long localId;

    private Long usuarioId;

    private LocalDateTime apertura;

    private LocalDateTime cierre;

    private BigDecimal montoInicial;

    private BigDecimal montoEsperado;

    private BigDecimal montoContado;

    private BigDecimal diferencia;

    private String observacion;

    private Long version;
}
