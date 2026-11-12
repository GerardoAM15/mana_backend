package pe.edu.upeu.mana.service.service;

import java.util.List;
import pe.edu.upeu.mana.dto.menu.PrecioSueltoRequestDTO;
import pe.edu.upeu.mana.dto.menu.PrecioSueltoResponseDTO;

public interface PrecioSueltoService {

    PrecioSueltoResponseDTO crear(PrecioSueltoRequestDTO r);

    PrecioSueltoResponseDTO actualizar(String servicio, String categoria, PrecioSueltoRequestDTO r);

    List<PrecioSueltoResponseDTO> listar();

    void eliminar(String servicio, String categoria);
}
