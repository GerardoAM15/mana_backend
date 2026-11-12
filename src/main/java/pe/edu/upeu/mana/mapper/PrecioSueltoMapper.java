package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.PrecioSuelto;
import pe.edu.upeu.mana.dto.menu.PrecioSueltoRequestDTO;
import pe.edu.upeu.mana.dto.menu.PrecioSueltoResponseDTO;

@Component
public class PrecioSueltoMapper {

    public PrecioSueltoResponseDTO toResponse(PrecioSuelto e) {
        return new PrecioSueltoResponseDTO(e.getServicio(), e.getCategoria(), e.getPrecio(), e.getRecargoLlevar(), e.getActivo());
    }

    public void actualizar(PrecioSuelto e, PrecioSueltoRequestDTO r) {
        e.setServicio(r.getServicio());
        e.setCategoria(r.getCategoria());
        e.setPrecio(r.getPrecio().setScale(2, java.math.RoundingMode.HALF_UP));
        e.setRecargoLlevar(r.getRecargoLlevar().setScale(2, java.math.RoundingMode.HALF_UP));
        e.setActivo(r.getActivo());
    }
}
