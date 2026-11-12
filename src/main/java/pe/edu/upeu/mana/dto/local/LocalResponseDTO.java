package pe.edu.upeu.mana.dto.local;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocalResponseDTO {

    private Long id;

    private String nombre;

    private String direccion;

    private String telefono;

    private Boolean activo;

    private Long version;
}
