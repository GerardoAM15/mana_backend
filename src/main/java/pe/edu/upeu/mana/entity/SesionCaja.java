package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sesiones_caja")
public class SesionCaja extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "local_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long localId;

    @Column(name = "usuario_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long usuarioId;

    @Column(name = "apertura", nullable = false, columnDefinition = "timestamp(9)")
    private LocalDateTime apertura;

    @Column(name = "cierre", nullable = true, columnDefinition = "timestamp(9)")
    private LocalDateTime cierre;

    @Column(name = "monto_inicial", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal montoInicial;

    @Column(name = "monto_esperado", nullable = true, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal montoEsperado;

    @Column(name = "monto_contado", nullable = true, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal montoContado;

    @Column(name = "diferencia", nullable = true, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal diferencia;

    @Column(name = "observacion", nullable = true, length = 250)
    private String observacion;

    @Column(name = "clave_abierta", nullable = true, length = 80)
    private String claveAbierta;
}
