package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "platos")
public class Plato extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "categoria", nullable = false, length = 32)
    private String categoria;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "activo", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean activo = true;
}
