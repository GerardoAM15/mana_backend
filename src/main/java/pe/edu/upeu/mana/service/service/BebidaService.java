package pe.edu.upeu.mana.service.service;

import java.util.List;
import java.time.LocalDate;
import pe.edu.upeu.mana.dto.bebida.BebidaRequestDTO;
import pe.edu.upeu.mana.dto.bebida.BebidaResponseDTO;

public interface BebidaService {

    BebidaResponseDTO crear(BebidaRequestDTO r);

    BebidaResponseDTO actualizar(Long id, BebidaRequestDTO r);

    BebidaResponseDTO buscar(Long id);

    List<BebidaResponseDTO> listar();

    void eliminar(Long id);
}
