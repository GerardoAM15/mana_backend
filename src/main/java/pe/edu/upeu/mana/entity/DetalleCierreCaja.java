package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detalles_cierre_caja")
public class DetalleCierreCaja extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "sesion_caja_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long sesionCajaId;

    @Column(name = "metodo", nullable = false, length = 32)
    private String metodo;

    @Column(name = "monto_esperado", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal montoEsperado;

    @Column(name = "monto_contado", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal montoContado;

    @Column(name = "diferencia", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal diferencia;
}
