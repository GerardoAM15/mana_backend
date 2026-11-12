package pe.edu.upeu.mana.dto.contrato;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.mana.enums.EstadoContrato;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PensionistaResponseDTO {

    private Long clienteId;

    private String dni;

    private String nombreCompleto;

    private Long contratoId;

    private String numero;

    private EstadoContrato estado;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private List<DetalleContratoResponseDTO> servicios;
}
