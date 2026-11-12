package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detalles_contrato")
public class DetalleContrato extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "contrato_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long contratoId;

    @Column(name = "servicio", nullable = false, length = 32)
    private String servicio;

    @Column(name = "raciones_contratadas", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long racionesContratadas;

    @Column(name = "saldo_raciones", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long saldoRaciones;

    @Column(name = "precio_racion_pactado", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal precioRacionPactado;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal subtotal;
}
