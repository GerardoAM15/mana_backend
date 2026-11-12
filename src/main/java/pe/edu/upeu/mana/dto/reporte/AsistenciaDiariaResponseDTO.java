package pe.edu.upeu.mana.dto.reporte;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaDiariaResponseDTO {

    private LocalDate fecha;

    private String servicio;

    private Long consumos;
}
