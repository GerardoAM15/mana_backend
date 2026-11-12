package pe.edu.upeu.mana.entity;

import pe.edu.upeu.mana.enums.TipoPagoContrato;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pagos_contrato")
public class PagoContrato extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "contrato_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long contratoId;

    @Column(name = "caja_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long cajaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 16, columnDefinition = "varchar2(16 char)")
    private TipoPagoContrato tipo;

    @Column(name = "monto", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal monto;

    @Column(name = "metodo", nullable = false, length = 32)
    private String metodo;

    @Column(name = "fecha", nullable = false, columnDefinition = "timestamp(9)")
    private LocalDateTime fecha;

    @Column(name = "referencia", nullable = true, length = 160)
    private String referencia;
}
