package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "id")
public class Cliente extends Persona {

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "activo", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean activo = true;
}
