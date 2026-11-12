package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.GastoDiario;
import pe.edu.upeu.mana.dto.gasto.GastoDiarioRequestDTO;
import pe.edu.upeu.mana.dto.gasto.GastoDiarioResponseDTO;

@Component
public class GastoDiarioMapper {

    public GastoDiarioResponseDTO toResponse(GastoDiario e) {
        return new GastoDiarioResponseDTO(e.getId(), e.getLocalId(), e.getFecha(), e.getMonto(), e.getDescripcion(), e.getActivo(), e.getVersion());
    }

    public void actualizar(GastoDiario e, GastoDiarioRequestDTO r) {
        e.setFecha(r.getFecha());
        e.setMonto(r.getMonto().setScale(2, java.math.RoundingMode.HALF_UP));
        e.setDescripcion(r.getDescripcion());
        e.setActivo(r.getActivo());
    }
}
