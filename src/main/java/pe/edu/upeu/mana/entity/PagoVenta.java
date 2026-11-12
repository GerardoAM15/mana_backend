package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pagos_venta")
public class PagoVenta extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "venta_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long ventaId;

    @Column(name = "monto", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal monto;

    @Column(name = "metodo", nullable = false, length = 32)
    private String metodo;

    @Column(name = "fecha", nullable = false, columnDefinition = "timestamp(9)")
    private LocalDateTime fecha;
}
