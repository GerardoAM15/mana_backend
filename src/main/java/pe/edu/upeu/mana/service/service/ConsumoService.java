package pe.edu.upeu.mana.service.service;

import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.consumo.ConsumoRequestDTO;
import pe.edu.upeu.mana.dto.consumo.ConsumoResponseDTO;

public interface ConsumoService {

    pe.edu.upeu.mana.dto.PaginaResponseDTO<ConsumoResponseDTO> listar(Long contratoId, String servicio, java.time.LocalDate desde, java.time.LocalDate hasta, Long localId, int pagina, int tamanio, String ordenarPor, String direccion);

    java.util.List<ConsumoResponseDTO> listarPorContrato(Long contratoId);

    ConsumoResponseDTO crear(ConsumoRequestDTO r);

    ConsumoResponseDTO anular(Long id, AnularRequestDTO r);
}
