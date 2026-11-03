package pe.edu.upeu.mana.dto.inventario;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompraBebidaRequestDTO {

    @NotNull(message = "Proveedor obligatorio")
    @Positive(message = "Proveedor inválido")
    private Long proveedorId;

    @NotNull(message = "Fecha obligatoria")
    private LocalDate fecha;

    @NotEmpty(message = "Agregue detalles")
    @Size(max = 100, message = "Máximo 100 detalles")
    private List<@NotNull(message = "Detalle obligatorio") @Valid DetalleCompraRequestDTO> detalles;
}
