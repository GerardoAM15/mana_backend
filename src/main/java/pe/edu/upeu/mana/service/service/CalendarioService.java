package pe.edu.upeu.mana.service.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.mana.dto.calendario.DiaNoAtencionRequestDTO;
import pe.edu.upeu.mana.dto.calendario.DiaNoAtencionResponseDTO;

public interface CalendarioService {

    DiaNoAtencionResponseDTO crear(DiaNoAtencionRequestDTO r);

    DiaNoAtencionResponseDTO actualizar(Long id, DiaNoAtencionRequestDTO r);

    List<DiaNoAtencionResponseDTO> listar(Long localId, LocalDate desde, LocalDate hasta);

    void eliminar(Long id);
}
