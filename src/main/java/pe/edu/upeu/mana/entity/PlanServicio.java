package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "plan_servicios")
@IdClass(PlanServicioId.class)
public class PlanServicio {

    @Id
    @Column(name = "plan_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long planId;

    @Id
    @Column(name = "servicio", nullable = false, length = 32)
    private String servicio;
}
