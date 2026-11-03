package pe.edu.upeu.mana.entity;

import pe.edu.upeu.mana.enums.TipoCorrelativo;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CorrelativoId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long localId;

    private TipoCorrelativo tipo;
}
