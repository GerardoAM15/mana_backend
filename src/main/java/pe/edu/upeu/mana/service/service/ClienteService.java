package pe.edu.upeu.mana.service.service;

import java.util.List;
import pe.edu.upeu.mana.dto.cliente.ClienteRequestDTO;
import pe.edu.upeu.mana.dto.cliente.ClienteResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

public interface ClienteService {

    ClienteResponseDTO crear(ClienteRequestDTO r);

    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO r);

    ClienteResponseDTO buscar(Long id);

    PaginaResponseDTO<ClienteResponseDTO> listar(String q, int pagina, int tamanio, String ordenarPor, String direccion);

    void eliminar(Long id);
}
