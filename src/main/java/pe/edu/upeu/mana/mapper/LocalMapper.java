package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.Local;
import pe.edu.upeu.mana.dto.local.LocalRequestDTO;
import pe.edu.upeu.mana.dto.local.LocalResponseDTO;

@Component
public class LocalMapper {

    public LocalResponseDTO toResponse(Local e) {
        return new LocalResponseDTO(e.getId(), e.getNombre(), e.getDireccion(), e.getTelefono(), e.getActivo(), e.getVersion());
    }

    public void actualizar(Local e, LocalRequestDTO r) {
        e.setNombre(r.getNombre());
        e.setDireccion(r.getDireccion());
        e.setTelefono(r.getTelefono());
        e.setActivo(r.getActivo());
    }
}
