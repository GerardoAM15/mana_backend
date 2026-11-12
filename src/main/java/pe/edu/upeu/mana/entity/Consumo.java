package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "consumos")
public class Consumo extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "detalle_contrato_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long detalleContratoId;

    @Column(name = "fecha", nullable = false, columnDefinition = "date")
    private LocalDate fecha;

    @Column(name = "registrado_en", nullable = false, columnDefinition = "timestamp(9)")
    private LocalDateTime registradoEn;

    @Column(name = "valor_racion", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal valorRacion;

    @Column(name = "clave_diaria", nullable = true, length = 100)
    private String claveDiaria;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "para_llevar", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean paraLlevar = false;

    @Column(name = "recargo_llevar", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal recargoLlevar;

    @Column(name = "metodo_recargo", nullable = true, length = 32)
    private String metodoRecargo;

    @Column(name = "caja_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long cajaId;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "anulado", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean anulado = false;

    @Column(name = "anulado_en", nullable = true, columnDefinition = "timestamp(9)")
    private LocalDateTime anuladoEn;

    @Column(name = "motivo_anulacion", nullable = true, length = 250)
    private String motivoAnulacion;
}
