package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.dto.auth.LoginResponseDTO;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.security.PermisosPorRol;

@Component
public class UsuarioMapper {

    public LoginResponseDTO toResponse(Usuario u, Empleado e, Local l, String token, Long segundos) {
        return new LoginResponseDTO(token, token == null ? null : "Bearer", segundos, u.getId(), e.getNombre() + " " + e.getApellido(), u.getRol().name(), l.getId(), l.getNombre(), PermisosPorRol.permisos(u.getRol().name()));
    }
}
