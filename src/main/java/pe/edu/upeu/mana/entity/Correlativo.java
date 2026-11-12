package pe.edu.upeu.mana.entity;

import pe.edu.upeu.mana.enums.TipoCorrelativo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "correlativos")
@IdClass(CorrelativoId.class)
public class Correlativo {

    @Id
    @Column(name = "local_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long localId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 16, columnDefinition = "varchar2(16 char)")
    private TipoCorrelativo tipo;

    @Column(name = "ultimo_numero", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long ultimoNumero = 0L;
}
