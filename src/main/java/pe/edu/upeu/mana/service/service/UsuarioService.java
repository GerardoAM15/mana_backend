package pe.edu.upeu.mana.service.service;

import java.util.List;
import pe.edu.upeu.mana.dto.usuario.RolRequestDTO;
import pe.edu.upeu.mana.dto.usuario.UsuarioRequestDTO;
import pe.edu.upeu.mana.dto.usuario.UsuarioResponseDTO;

public interface UsuarioService {

    UsuarioResponseDTO crear(UsuarioRequestDTO r);

    UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO r);

    UsuarioResponseDTO buscar(Long id);

    List<UsuarioResponseDTO> listar();

    UsuarioResponseDTO cambiarRol(Long id, RolRequestDTO r);

    void eliminar(Long id);
}
