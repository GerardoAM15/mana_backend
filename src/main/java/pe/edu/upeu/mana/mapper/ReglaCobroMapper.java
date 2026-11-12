package pe.edu.upeu.mana.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.menu.ComponenteReglaResponseDTO;
import pe.edu.upeu.mana.dto.menu.ReglaCobroResponseDTO;

@Component
public class ReglaCobroMapper {

    public ReglaCobroResponseDTO toResponse(ReglaCobro e, List<ComponenteRegla> componentes) {
        return new ReglaCobroResponseDTO(e.getId(), e.getNombre(), e.getServicio(), e.getPrecioCombo(), e.getRecargoLlevar(), e.getActivo(), componentes.stream().sorted(java.util.Comparator.comparing(ComponenteRegla::getCategoria)).map(c -> new ComponenteReglaResponseDTO(c.getCategoria(), c.getCantidad())).toList(), e.getVersion());
    }
}
