package pe.edu.upeu.mana.service.service;

import java.util.List;
import pe.edu.upeu.mana.dto.local.LocalRequestDTO;
import pe.edu.upeu.mana.dto.local.LocalResponseDTO;

public interface LocalService {

    LocalResponseDTO crear(LocalRequestDTO r);

    LocalResponseDTO actualizar(Long id, LocalRequestDTO r);

    LocalResponseDTO buscar(Long id);

    List<LocalResponseDTO> listar();

    void eliminar(Long id);
}
