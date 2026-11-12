package pe.edu.upeu.mana.dto.venta;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatoComboResponseDTO {

    private Long platoId;

    private String nombre;

    private Long cantidad;
}
