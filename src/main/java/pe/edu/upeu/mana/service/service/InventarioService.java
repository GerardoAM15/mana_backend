package pe.edu.upeu.mana.service.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.mana.dto.inventario.CompraBebidaRequestDTO;
import pe.edu.upeu.mana.dto.inventario.CompraBebidaResponseDTO;
import pe.edu.upeu.mana.dto.inventario.MovimientoStockResponseDTO;
import pe.edu.upeu.mana.dto.inventario.StockBebidaResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

public interface InventarioService {

    CompraBebidaResponseDTO crear(CompraBebidaRequestDTO r);

    CompraBebidaResponseDTO buscar(Long id);

    List<CompraBebidaResponseDTO> listar(Long localId, LocalDate desde, LocalDate hasta);

    List<StockBebidaResponseDTO> listarStock(Long localId);

    PaginaResponseDTO<MovimientoStockResponseDTO> listarMovimientos(Long localId, Long bebidaId, LocalDate desde, LocalDate hasta, int pagina, int tamanio, String ordenarPor, String direccion);
}
