package pe.edu.upeu.mana.service.service;

import java.util.List;
import pe.edu.upeu.mana.dto.menu.TarifaServicioRequestDTO;
import pe.edu.upeu.mana.dto.menu.TarifaServicioResponseDTO;

public interface TarifaServicioService {

    TarifaServicioResponseDTO crear(TarifaServicioRequestDTO r);

    TarifaServicioResponseDTO actualizar(Long id, TarifaServicioRequestDTO r);

    TarifaServicioResponseDTO buscar(Long id);

    List<TarifaServicioResponseDTO> listar();

    void eliminar(Long id);
}
