package pe.edu.upeu.mana.dto.cliente;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {

    private Long id;

    private String dni;

    private String nombre;

    private String apellido;

    private String telefono;

    private Boolean activo;

    private Long version;
}
