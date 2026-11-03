package pe.edu.upeu.mana.dto.contrato;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import pe.edu.upeu.mana.enums.EstadoContrato;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pe.edu.upeu.mana.dto.consumo.ConsumoResponseDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContratoResponseDTO {

    private Long id;

    private String numero;

    private Long localId;

    private Long clienteId;

    private String dni;

    private String clienteNombre;

    private Long planId;

    private String planNombre;

    private EstadoContrato estado;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private BigDecimal montoTotal;

    private Long anteriorId;

    private LocalDateTime anuladoEn;

    private String motivoAnulacion;

    private List<DetalleContratoResponseDTO> detalles;

    private List<PagoContratoResponseDTO> pagos;

    private List<ConsumoResponseDTO> consumos;

    private Long version;
}
