package pe.edu.upeu.mana.dto.proveedor;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorResponseDTO {

    private Long id;

    private String razonSocial;

    private String telefono;

    private Boolean activo;

    private List<Long> bebidaIds;

    private Long version;
}
