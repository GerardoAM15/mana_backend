package pe.edu.upeu.mana.dto.reporte;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatoConsumidoResponseDTO {

    private Long platoId;

    private String platoNombre;

    private String servicio;

    private Long cantidad;
}
