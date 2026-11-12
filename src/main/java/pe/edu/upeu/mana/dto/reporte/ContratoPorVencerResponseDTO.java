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
public class ContratoPorVencerResponseDTO {

    private Long contratoId;

    private String numero;

    private String dni;

    private String clienteNombre;

    private LocalDate fechaFin;

    private String servicio;

    private Long saldoRaciones;
}
