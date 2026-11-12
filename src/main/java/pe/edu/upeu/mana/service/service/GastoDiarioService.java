package pe.edu.upeu.mana.service.service;

import java.util.List;
import java.time.LocalDate;
import pe.edu.upeu.mana.dto.gasto.GastoDiarioRequestDTO;
import pe.edu.upeu.mana.dto.gasto.GastoDiarioResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

public interface GastoDiarioService {

    GastoDiarioResponseDTO crear(GastoDiarioRequestDTO r);

    GastoDiarioResponseDTO actualizar(Long id, GastoDiarioRequestDTO r);

    GastoDiarioResponseDTO buscar(Long id);

    PaginaResponseDTO<GastoDiarioResponseDTO> listar(Long localId, LocalDate desde, LocalDate hasta, int pagina, int tamanio, String ordenarPor, String direccion);

    void eliminar(Long id);
}
