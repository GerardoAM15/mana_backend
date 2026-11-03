package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "precios_sueltos")
@IdClass(PrecioSueltoId.class)
public class PrecioSuelto {

    @Id
    @Column(name = "servicio", nullable = false, length = 32)
    private String servicio;

    @Id
    @Column(name = "categoria", nullable = false, length = 32)
    private String categoria;

    @Column(name = "precio", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal precio;

    @Column(name = "recargo_llevar", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal recargoLlevar;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "activo", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean activo = true;
}
