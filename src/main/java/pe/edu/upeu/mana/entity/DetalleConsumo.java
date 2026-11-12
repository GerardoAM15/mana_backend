package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detalles_consumo")
public class DetalleConsumo extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "consumo_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long consumoId;

    @Column(name = "plato_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long platoId;

    @Column(name = "categoria_consumida", nullable = false, length = 32)
    private String categoriaConsumida;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long cantidad;
}
