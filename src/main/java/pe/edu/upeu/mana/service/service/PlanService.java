package pe.edu.upeu.mana.service.service;

import java.util.List;
import pe.edu.upeu.mana.dto.plan.PlanRequestDTO;
import pe.edu.upeu.mana.dto.plan.PlanResponseDTO;

public interface PlanService {

    PlanResponseDTO crear(PlanRequestDTO r);

    PlanResponseDTO actualizar(Long id, PlanRequestDTO r);

    PlanResponseDTO buscar(Long id);

    List<PlanResponseDTO> listar();

    void eliminar(Long id);
}
