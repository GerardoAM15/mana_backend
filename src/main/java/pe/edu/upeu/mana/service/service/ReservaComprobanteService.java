package pe.edu.upeu.mana.service.service;
import pe.edu.upeu.mana.dto.comprobante.ComprobanteRequestDTO;
public interface ReservaComprobanteService {
    Long venta(Long id, ComprobanteRequestDTO solicitud);
    Long contrato(Long id, ComprobanteRequestDTO solicitud);
    void anularVenta(Long id);
    void anularContrato(Long id);
}
