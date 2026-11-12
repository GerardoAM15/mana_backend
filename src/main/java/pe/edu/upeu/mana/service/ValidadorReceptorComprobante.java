package pe.edu.upeu.mana.service;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.dto.comprobante.ComprobanteRequestDTO;
import pe.edu.upeu.mana.enums.TipoSolicitudComprobante;
import pe.edu.upeu.mana.exception.DatosInvalidosException;
import pe.edu.upeu.mana.service.service.ConsultaDocumentoService;
@Component
public class ValidadorReceptorComprobante {
    private final FacturacionProperties config;
    private final ConsultaDocumentoService consultas;
    public ValidadorReceptorComprobante(FacturacionProperties config, ConsultaDocumentoService consultas) { this.config=config; this.consultas=consultas; }
    public record Receptor(String tipo, String numero, String nombre, String direccion) {}
    public Receptor validar(ComprobanteRequestDTO r, BigDecimal total) {
        if (r == null || r.getTipo() == null || r.getTipo() == TipoSolicitudComprobante.TICKET) throw new DatosInvalidosException("Seleccione boleta o factura");
        String tipo = r.getReceptorTipoDoc() == null ? "0" : r.getReceptorTipoDoc();
        String num = texto(r.getReceptorNumDoc()), nombre = texto(r.getReceptorNombre());
        if (!tipo.matches("[016]")) throw new DatosInvalidosException("Tipo de documento inválido");
        if (r.getTipo() == TipoSolicitudComprobante.FACTURA && !tipo.equals("6")) throw new DatosInvalidosException("La factura requiere RUC");
        if (tipo.equals("1") && !num.matches("[0-9]{8}")) throw new DatosInvalidosException("El DNI debe tener 8 dígitos");
        if (tipo.equals("6") && !num.matches("(10|15|17|20)[0-9]{9}")) throw new DatosInvalidosException("El RUC debe tener 11 dígitos y empezar con 10, 15, 17 o 20");
        if (r.getTipo() == TipoSolicitudComprobante.BOLETA && total.compareTo(config.getBoletaMontoIdentificacion()) >= 0 && tipo.equals("0"))
            throw new DatosInvalidosException("La boleta por este monto requiere DNI o RUC");
        if (tipo.equals("0")) { num = null; nombre = "CLIENTES VARIOS"; }
        else if (nombre.isBlank() && tipo.equals("6")) nombre = consultas.consultarRuc(num).getRazonSocial();
        if (nombre.isBlank()) throw new DatosInvalidosException("El nombre del receptor es obligatorio");
        return new Receptor(tipo, num, nombre, r.getReceptorDireccion());
    }
    private String texto(String s) { return s == null ? "" : s.trim(); }
}

