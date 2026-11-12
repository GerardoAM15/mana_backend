package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "servicio_dias_atencion")
@IdClass(ServicioDiaAtencionId.class)
public class ServicioDiaAtencion {

    @Id
    @Column(name = "servicio", nullable = false, length = 32)
    private String servicio;

    @Id
    @Column(name = "dia_semana", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Integer diaSemana;
}
