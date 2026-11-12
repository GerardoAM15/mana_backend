package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detalles_compra")
public class DetalleCompra extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "compra_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long compraId;

    @Column(name = "bebida_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long bebidaId;

    @Column(name = "presentaciones", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long presentaciones;

    @Column(name = "unidades_por_presentacion", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long unidadesPorPresentacion;

    @Column(name = "costo_total", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal costoTotal;
}
