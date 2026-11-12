package pe.edu.upeu.mana.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import pe.edu.upeu.mana.enums.EstadoVenta;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponseDTO {

    private Long id;

    private String numero;

    private Long localId;

    private Long cajaId;

    private Long clienteId;

    private String servicio;

    private Boolean paraLlevar;

    private EstadoVenta estado;

    private LocalDateTime fecha;

    private BigDecimal recargoLlevar;

    private BigDecimal total;

    private LocalDateTime anuladaEn;

    private String motivoAnulacion;

    private List<LineaVentaResponseDTO> lineas;

    private List<PagoVentaResponseDTO> pagos;

    private List<DevolucionVentaResponseDTO> devoluciones;

    private Long version;
}
