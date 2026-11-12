package pe.edu.upeu.mana.dto.catalogo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoResponseDTO {

    private String codigo;

    private String nombre;

    private String descripcion;

    private Boolean activo;
}
