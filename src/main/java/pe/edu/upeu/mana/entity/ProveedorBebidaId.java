package pe.edu.upeu.mana.entity;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProveedorBebidaId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long proveedorId;

    private Long bebidaId;
}
