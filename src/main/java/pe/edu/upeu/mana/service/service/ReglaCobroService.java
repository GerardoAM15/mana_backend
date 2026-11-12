package pe.edu.upeu.mana.service.service;

import java.util.List;
import pe.edu.upeu.mana.dto.menu.ReglaCobroRequestDTO;
import pe.edu.upeu.mana.dto.menu.ReglaCobroResponseDTO;

public interface ReglaCobroService {

    ReglaCobroResponseDTO crear(ReglaCobroRequestDTO r);

    ReglaCobroResponseDTO actualizar(Long id, ReglaCobroRequestDTO r);

    ReglaCobroResponseDTO buscar(Long id);

    List<ReglaCobroResponseDTO> listar();

    void eliminar(Long id);
}
