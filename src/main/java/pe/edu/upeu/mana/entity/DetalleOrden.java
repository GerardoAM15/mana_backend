package pe.edu.upeu.mana.entity;

import pe.edu.upeu.mana.enums.TipoLineaVenta;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detalles_orden")
public class DetalleOrden extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "venta_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long ventaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 16, columnDefinition = "varchar2(16 char)")
    private TipoLineaVenta tipo;

    @Column(name = "plato_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long platoId;

    @Column(name = "combo_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long comboId;

    @Column(name = "bebida_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long bebidaId;

    @Column(name = "descripcion", nullable = false, length = 160)
    private String descripcion;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal precioUnitario;

    @Column(name = "recargo_unitario", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal recargoUnitario;
}
