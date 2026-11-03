package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.DiaNoAtencion;
import pe.edu.upeu.mana.dto.calendario.DiaNoAtencionResponseDTO;

@Component
public class DiaNoAtencionMapper {

    public DiaNoAtencionResponseDTO toResponse(DiaNoAtencion e) {
        return new DiaNoAtencionResponseDTO(e.getId(), e.getLocalId(), e.getFecha(), e.getMotivo(), e.getVersion());
    }
}
