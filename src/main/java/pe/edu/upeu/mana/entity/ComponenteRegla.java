package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "componentes_regla")
@IdClass(ComponenteReglaId.class)
public class ComponenteRegla {

    @Id
    @Column(name = "regla_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long reglaId;

    @Id
    @Column(name = "categoria", nullable = false, length = 32)
    private String categoria;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long cantidad;
}
