package pe.edu.upeu.mana.dto.menu;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuPlatoResponseDTO {

    private Long platoId;

    private String nombre;

    private String categoria;
}
