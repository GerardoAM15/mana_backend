package pe.edu.upeu.mana.config;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.enums.AmbienteFacturacion;
@Getter @Setter @Component
@ConfigurationProperties(prefix = "app.facturacion")
public class FacturacionProperties {
    private boolean habilitada;
    private AmbienteFacturacion ambiente = AmbienteFacturacion.BETA;
    private String url = "https://facturacion.apisperu.com/api/v1";
    private String token = "";
    private Long companyId;
    private BigDecimal igvPorcentaje = new BigDecimal("18.00");
    private BigDecimal boletaMontoIdentificacion = new BigDecimal("700.00");
    private Empresa empresa = new Empresa();
    @Getter @Setter
    public static class Empresa {
        private String ruc = "";
        private String razonSocial = "";
        private String nombreComercial = "";
        private String direccion = "";
        private String ubigeo = "";
        // Sin estos tres SUNAT acepta con observaciones 4096/4097/4098 (domicilio fiscal incompleto).
        private String departamento = "";
        private String provincia = "";
        private String distrito = "";
        // Código de establecimiento anexo en SUNAT; "0000" = domicilio fiscal.
        private String codLocal = "0000";
    }
}
