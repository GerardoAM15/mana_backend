package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.mana.enums.*;

@Getter
@Setter
@Entity
@Table(name = "comprobante_detalles")
public class ComprobanteDetalle extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "comprobante_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long comprobanteId;

    @Column(name = "item", nullable = false, precision = 5, scale = 0, columnDefinition = "number(5,0)")
    private Integer item;

    @Column(name = "codigo", nullable = true, length = 30, columnDefinition = "varchar2(30 char)")
    private String codigo;

    @Column(name = "descripcion", nullable = false, length = 250, columnDefinition = "varchar2(250 char)")
    private String descripcion;

    @Column(name = "unidad", nullable = false, length = 3, columnDefinition = "varchar2(3 char)")
    private String unidad;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3, columnDefinition = "number(12,3)")
    private BigDecimal cantidad;

    @Column(name = "valor_unitario", nullable = false, precision = 14, scale = 4, columnDefinition = "number(14,4)")
    private BigDecimal valorUnitario;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 4, columnDefinition = "number(14,4)")
    private BigDecimal precioUnitario;

    @Column(name = "valor_venta", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal valorVenta;

    @Column(name = "igv", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal igv;

    @Column(name = "total", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal total;
}
