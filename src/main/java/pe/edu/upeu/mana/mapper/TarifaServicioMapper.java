package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.TarifaServicio;
import pe.edu.upeu.mana.dto.menu.TarifaServicioRequestDTO;
import pe.edu.upeu.mana.dto.menu.TarifaServicioResponseDTO;

@Component
public class TarifaServicioMapper {

    public TarifaServicioResponseDTO toResponse(TarifaServicio e) {
        return new TarifaServicioResponseDTO(e.getId(), e.getServicio(), e.getPrecio(), e.getActivo(), e.getVersion());
    }

    public void actualizar(TarifaServicio e, TarifaServicioRequestDTO r) {
        e.setServicio(r.getServicio());
        e.setPrecio(r.getPrecio().setScale(2, java.math.RoundingMode.HALF_UP));
        e.setActivo(r.getActivo());
    }
}
