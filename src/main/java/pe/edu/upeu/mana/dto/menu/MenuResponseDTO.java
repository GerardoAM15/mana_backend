package pe.edu.upeu.mana.dto.menu;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponseDTO {

    private Long id;

    private Long localId;

    private LocalDate fecha;

    private String servicio;

    private List<MenuPlatoResponseDTO> platos;

    private Long version;
}
