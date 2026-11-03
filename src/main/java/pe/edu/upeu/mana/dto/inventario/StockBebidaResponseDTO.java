package pe.edu.upeu.mana.dto.inventario;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockBebidaResponseDTO {

    private Long id;

    private Long localId;

    private Long bebidaId;

    private String bebidaNombre;

    private Long stock;
}
