package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.usuario.UsuarioResponseDTO;

@Component
public class UsuarioGestionMapper {

    public UsuarioResponseDTO toResponse(Usuario u, Empleado e, Local l, boolean gestionable) {
        return new UsuarioResponseDTO(u.getId(), e.getId(), e.getDni(), e.getNombre(), e.getApellido(), e.getTelefono(), l.getId(), l.getNombre(), e.getCargo(), u.getCorreo(), u.getRol(), u.getActivo(), u.getVersion(), gestionable);
    }
}
