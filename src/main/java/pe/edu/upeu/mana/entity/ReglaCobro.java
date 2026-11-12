package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reglas_cobro")
public class ReglaCobro extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "servicio", nullable = false, length = 32)
    private String servicio;

    @Column(name = "precio_combo", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal precioCombo;

    @Column(name = "recargo_llevar", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal recargoLlevar;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "activo", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean activo = true;
}
