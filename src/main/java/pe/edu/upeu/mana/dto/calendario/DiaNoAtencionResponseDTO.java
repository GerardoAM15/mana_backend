package pe.edu.upeu.mana.dto.calendario;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaNoAtencionResponseDTO {

    private Long id;

    private Long localId;

    private LocalDate fecha;

    private String motivo;

    private Long version;
}
