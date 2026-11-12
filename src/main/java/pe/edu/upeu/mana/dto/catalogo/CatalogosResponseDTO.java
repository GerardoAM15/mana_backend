package pe.edu.upeu.mana.dto.catalogo;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogosResponseDTO {

    private List<CatalogoResponseDTO> servicios;

    private List<CatalogoResponseDTO> metodosPago;

    private List<CatalogoResponseDTO> categorias;

    private List<CatalogoResponseDTO> roles;

    private List<DiaAtencionResponseDTO> diasAtencion;
}
