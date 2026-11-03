package pe.edu.upeu.mana.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.plan.PlanResponseDTO;

@Component
public class PlanMapper {

    public PlanResponseDTO toResponse(TipoPlan e, List<PlanServicio> servicios) {
        return new PlanResponseDTO(e.getId(), e.getNombre(), e.getPeriodo(), e.getActivo(), servicios.stream().map(PlanServicio::getServicio).sorted().toList(), e.getVersion());
    }
}
