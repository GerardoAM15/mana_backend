package pe.edu.upeu.mana.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.menu.MenuPlatoResponseDTO;
import pe.edu.upeu.mana.dto.menu.MenuResponseDTO;

@Component
public class MenuMapper {

    public MenuResponseDTO toResponse(ProgramacionMenu e, List<Plato> platos) {
        return new MenuResponseDTO(e.getId(), e.getLocalId(), e.getFecha(), e.getServicio(), platos.stream().map(p -> new MenuPlatoResponseDTO(p.getId(), p.getNombre(), p.getCategoria())).toList(), e.getVersion());
    }
}
