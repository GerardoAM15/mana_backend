package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detalle_combo_platos")
@IdClass(DetalleComboPlatoId.class)
public class DetalleComboPlato {

    @Id
    @Column(name = "detalle_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long detalleId;

    @Id
    @Column(name = "plato_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long platoId;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long cantidad;
}
