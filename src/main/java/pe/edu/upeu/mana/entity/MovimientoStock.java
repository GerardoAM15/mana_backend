package pe.edu.upeu.mana.entity;

import pe.edu.upeu.mana.enums.TipoMovimientoStock;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "movimientos_stock")
public class MovimientoStock extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "local_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long localId;

    @Column(name = "bebida_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long bebidaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 16, columnDefinition = "varchar2(16 char)")
    private TipoMovimientoStock tipo;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long cantidad;

    @Column(name = "fecha", nullable = false, columnDefinition = "timestamp(9)")
    private LocalDateTime fecha;

    @Column(name = "detalle_compra_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long detalleCompraId;

    @Column(name = "detalle_orden_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long detalleOrdenId;

    @Column(name = "motivo", nullable = false, length = 250)
    private String motivo;
}
