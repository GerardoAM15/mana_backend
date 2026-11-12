package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.Bebida;
import pe.edu.upeu.mana.dto.bebida.BebidaRequestDTO;
import pe.edu.upeu.mana.dto.bebida.BebidaResponseDTO;

@Component
public class BebidaMapper {

    public BebidaResponseDTO toResponse(Bebida e) {
        return new BebidaResponseDTO(e.getId(), e.getNombre(), e.getPrecioVenta(), e.getActivo(), e.getVersion());
    }

    public void actualizar(Bebida e, BebidaRequestDTO r) {
        e.setNombre(r.getNombre());
        e.setPrecioVenta(r.getPrecioVenta().setScale(2, java.math.RoundingMode.HALF_UP));
        e.setActivo(r.getActivo());
    }
}
