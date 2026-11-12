package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gastos_diarios")
public class GastoDiario extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "local_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long localId;

    @Column(name = "fecha", nullable = false, columnDefinition = "date")
    private LocalDate fecha;

    @Column(name = "monto", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal monto;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "activo", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean activo = true;
}
