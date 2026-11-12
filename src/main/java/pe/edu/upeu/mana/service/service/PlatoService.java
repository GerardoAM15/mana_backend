package pe.edu.upeu.mana.service.service;

import java.util.List;
import pe.edu.upeu.mana.dto.menu.PlatoRequestDTO;
import pe.edu.upeu.mana.dto.menu.PlatoResponseDTO;

public interface PlatoService {

    PlatoResponseDTO crear(PlatoRequestDTO r);

    PlatoResponseDTO actualizar(Long id, PlatoRequestDTO r);

    PlatoResponseDTO buscar(Long id);

    List<PlatoResponseDTO> listar();

    void eliminar(Long id);
}
