package pe.edu.upeu.mana.service.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.mana.dto.menu.ClonarMenuRequestDTO;
import pe.edu.upeu.mana.dto.menu.CopiarCenaRequestDTO;
import pe.edu.upeu.mana.dto.menu.MenuRequestDTO;
import pe.edu.upeu.mana.dto.menu.MenuResponseDTO;

public interface MenuService {

    MenuResponseDTO guardar(MenuRequestDTO r);

    List<MenuResponseDTO> listar(Long localId, LocalDate desde, LocalDate hasta, String servicio);

    List<MenuResponseDTO> clonar(ClonarMenuRequestDTO r);

    MenuResponseDTO copiarACena(CopiarCenaRequestDTO r);
}
