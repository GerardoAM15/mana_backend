package pe.edu.upeu.mana.mapper;

import java.util.*;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.venta.DevolucionVentaResponseDTO;
import pe.edu.upeu.mana.dto.venta.LineaVentaResponseDTO;
import pe.edu.upeu.mana.dto.venta.PagoVentaResponseDTO;
import pe.edu.upeu.mana.dto.venta.PlatoComboResponseDTO;
import pe.edu.upeu.mana.dto.venta.VentaResponseDTO;

@Component
public class VentaMapper {

    public VentaResponseDTO toResponse(Venta v, List<LineaVentaResponseDTO> lineas, List<PagoVenta> pagos, List<DevolucionVenta> devoluciones) {
        return new VentaResponseDTO(v.getId(), v.getNumero(), v.getLocalId(), v.getCajaId(), v.getClienteId(), v.getServicio(), v.getParaLlevar(), v.getEstado(), v.getFecha(), v.getRecargoLlevar(), v.getTotal(), v.getAnuladaEn(), v.getMotivoAnulacion(), lineas, pagos.stream().map(p -> new PagoVentaResponseDTO(p.getId(), p.getMetodo(), p.getMonto(), p.getFecha())).toList(), devoluciones.stream().map(d -> new DevolucionVentaResponseDTO(d.getId(), d.getMetodo(), d.getMonto(), d.getFecha(), d.getMotivo())).toList(), v.getVersion());
    }

    public LineaVentaResponseDTO toLinea(DetalleOrden d, List<PlatoComboResponseDTO> platos) {
        return new LineaVentaResponseDTO(d.getId(), d.getTipo(), d.getPlatoId(), d.getComboId(), d.getBebidaId(), d.getDescripcion(), d.getCantidad(), d.getPrecioUnitario(), d.getRecargoUnitario(), platos);
    }
}
