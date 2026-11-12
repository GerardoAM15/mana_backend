package pe.edu.upeu.mana.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.inventario.CompraBebidaResponseDTO;
import pe.edu.upeu.mana.dto.inventario.DetalleCompraResponseDTO;

@Component
public class InventarioMapper {

    public CompraBebidaResponseDTO toResponse(CompraBebida c, String proveedor, List<DetalleCompraResponseDTO> detalles) {
        return new CompraBebidaResponseDTO(c.getId(), c.getLocalId(), c.getProveedorId(), proveedor, c.getFecha(), c.getTotal(), detalles);
    }

    public DetalleCompraResponseDTO toDetalle(DetalleCompra d, String nombre) {
        return new DetalleCompraResponseDTO(d.getId(), d.getBebidaId(), nombre, d.getPresentaciones(), d.getUnidadesPorPresentacion(), d.getPresentaciones() * d.getUnidadesPorPresentacion(), d.getCostoTotal());
    }
}
