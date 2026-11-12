package pe.edu.upeu.mana.dto.comprobante;
import jakarta.validation.constraints.*;
import lombok.*;
import pe.edu.upeu.mana.enums.TipoSolicitudComprobante;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ComprobanteRequestDTO {
    @NotNull(message = "Tipo de comprobante obligatorio")
    private TipoSolicitudComprobante tipo;
    @Pattern(regexp = "[016]", message = "Tipo de documento inválido")
    private String receptorTipoDoc;
    @Size(max = 15, message = "Documento de máximo 15 caracteres")
    private String receptorNumDoc;
    @Size(max = 160, message = "Nombre de máximo 160 caracteres")
    private String receptorNombre;
    @Size(max = 250, message = "Dirección de máximo 250 caracteres")
    private String receptorDireccion;
    // Opcional: si se indica, el PDF y el XML se envían a este correo cuando SUNAT acepta el comprobante.
    @Email(message = "Correo inválido")
    @Size(max = 120, message = "Correo de máximo 120 caracteres")
    private String receptorEmail;

    public ComprobanteRequestDTO(TipoSolicitudComprobante tipo, String receptorTipoDoc, String receptorNumDoc, String receptorNombre, String receptorDireccion) {
        this(tipo, receptorTipoDoc, receptorNumDoc, receptorNombre, receptorDireccion, null);
    }
}
