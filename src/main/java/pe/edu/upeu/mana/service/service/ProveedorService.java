package pe.edu.upeu.mana.service.service;

import java.util.List;
import pe.edu.upeu.mana.dto.proveedor.ProveedorRequestDTO;
import pe.edu.upeu.mana.dto.proveedor.ProveedorResponseDTO;

public interface ProveedorService {

    ProveedorResponseDTO crear(ProveedorRequestDTO r);

    ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO r);

    ProveedorResponseDTO buscar(Long id);

    List<ProveedorResponseDTO> listar();

    void eliminar(Long id);
}
