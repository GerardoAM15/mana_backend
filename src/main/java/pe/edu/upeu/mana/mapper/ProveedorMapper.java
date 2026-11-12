package pe.edu.upeu.mana.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.proveedor.ProveedorResponseDTO;

@Component
public class ProveedorMapper {

    public ProveedorResponseDTO toResponse(Proveedor p, List<ProveedorBebida> bebidas) {
        return new ProveedorResponseDTO(p.getId(), p.getRazonSocial(), p.getTelefono(), p.getActivo(), bebidas.stream().map(ProveedorBebida::getBebidaId).sorted().toList(), p.getVersion());
    }
}
