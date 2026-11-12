package pe.edu.upeu.mana.dto.consumo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleConsumoResponseDTO {

    private Long platoId;

    private String platoNombre;

    private String categoria;

    private Long cantidad;
}
