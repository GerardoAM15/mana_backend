package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.mana.enums.*;

@Getter
@Setter
@Entity
@Table(name = "series_comprobante")
public class SerieComprobante extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "local_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long localId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 24, columnDefinition = "varchar2(24 char)")
    private TipoSerie tipo;

    @Column(name = "serie", nullable = false, length = 4, columnDefinition = "varchar2(4 char)")
    private String serie;

    @Column(name = "ultimo_numero", nullable = false, precision = 8, scale = 0, columnDefinition = "number(8,0)")
    private Integer ultimoNumero = 0;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "activo", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean activo = true;
}
