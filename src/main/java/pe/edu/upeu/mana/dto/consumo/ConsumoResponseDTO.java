package pe.edu.upeu.mana.dto.consumo;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumoResponseDTO {

    private Long id;

    private Long contratoId;

    private Long detalleContratoId;

    private String servicio;

    private LocalDate fecha;

    private LocalDateTime registradoEn;

    private BigDecimal valorRacion;

    private Boolean paraLlevar;

    private BigDecimal recargoLlevar;

    private String metodoRecargo;

    private Long cajaId;

    private Boolean anulado;

    private LocalDateTime anuladoEn;

    private String motivoAnulacion;

    private List<DetalleConsumoResponseDTO> platos;
}
