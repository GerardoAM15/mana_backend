package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stock_bebidas")
public class StockBebida extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "local_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long localId;

    @Column(name = "bebida_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long bebidaId;

    @Column(name = "stock", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long stock;
}
