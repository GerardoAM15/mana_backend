package pe.edu.upeu.mana.dto.menu;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteReglaResponseDTO {

    private String categoria;

    private Long cantidad;
}
