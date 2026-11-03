package pe.edu.upeu.mana.dto.pos;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pe.edu.upeu.mana.dto.menu.MenuResponseDTO;
import pe.edu.upeu.mana.dto.menu.ReglaCobroResponseDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PosCatalogoResponseDTO {

    private Long localId;

    private LocalDate fecha;

    private String servicio;

    private MenuResponseDTO menu;

    private List<ReglaCobroResponseDTO> reglas;

    private List<PlatoSueltoPosResponseDTO> sueltos;

    private List<BebidaPosResponseDTO> bebidas;
}
