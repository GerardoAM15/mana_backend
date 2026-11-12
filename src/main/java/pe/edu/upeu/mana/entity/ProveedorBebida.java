package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "proveedor_bebidas")
@IdClass(ProveedorBebidaId.class)
public class ProveedorBebida {

    @Id
    @Column(name = "proveedor_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long proveedorId;

    @Id
    @Column(name = "bebida_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long bebidaId;
}
