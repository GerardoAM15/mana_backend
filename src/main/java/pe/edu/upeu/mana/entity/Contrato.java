package pe.edu.upeu.mana.entity;

import pe.edu.upeu.mana.enums.EstadoContrato;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contratos")
public class Contrato extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "numero", nullable = false, length = 12)
    private String numero;

    @Column(name = "local_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long localId;

    @Column(name = "cliente_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long clienteId;

    @Column(name = "plan_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long planId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 16, columnDefinition = "varchar2(16 char)")
    private EstadoContrato estado;

    @Column(name = "fecha_inicio", nullable = false, columnDefinition = "date")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false, columnDefinition = "date")
    private LocalDate fechaFin;

    @Column(name = "monto_total", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal montoTotal;

    @Column(name = "anterior_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long anteriorId;

    @Column(name = "anulado_en", nullable = true, columnDefinition = "timestamp(9)")
    private LocalDateTime anuladoEn;

    @Column(name = "motivo_anulacion", nullable = true, length = 250)
    private String motivoAnulacion;
}
