package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.Plato;
import pe.edu.upeu.mana.dto.menu.PlatoRequestDTO;
import pe.edu.upeu.mana.dto.menu.PlatoResponseDTO;

@Component
public class PlatoMapper {

    public PlatoResponseDTO toResponse(Plato e) {
        return new PlatoResponseDTO(e.getId(), e.getNombre(), e.getCategoria(), e.getActivo(), e.getVersion());
    }

    public void actualizar(Plato e, PlatoRequestDTO r) {
        e.setNombre(r.getNombre());
        e.setCategoria(r.getCategoria());
        e.setActivo(r.getActivo());
    }
}
