package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menu_platos")
@IdClass(MenuPlatoId.class)
public class MenuPlato {

    @Id
    @Column(name = "menu_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long menuId;

    @Id
    @Column(name = "plato_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long platoId;
}
