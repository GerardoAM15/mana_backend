package pe.edu.upeu.mana.service.service;
import java.time.LocalDate;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.dto.comprobante.*;
import pe.edu.upeu.mana.enums.*;
public interface ComprobanteService {
    PaginaResponseDTO<ComprobanteResponseDTO> listar(LocalDate desde,LocalDate hasta,TipoComprobante tipo,EstadoComprobante estado,Long localId,int pagina,int tamanio);
    ComprobanteConfiguracionDTO configuracion();
    ComprobanteResponseDTO buscar(Long id);
    ComprobanteResponseDTO venta(Long ventaId);
    ComprobanteResponseDTO contrato(Long contratoId);
    ComprobanteResponseDTO emitirVenta(Long ventaId,ComprobanteRequestDTO solicitud);
    ComprobanteResponseDTO emitirContrato(Long contratoId,ComprobanteRequestDTO solicitud);
    ComprobanteResponseDTO reenviar(Long id);
    ComprobanteResponseDTO enviarCorreo(Long id,String email);
    byte[] pdf(Long id);
    String xml(Long id);
}
