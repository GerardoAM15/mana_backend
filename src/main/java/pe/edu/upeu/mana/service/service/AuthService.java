package pe.edu.upeu.mana.service.service;

import pe.edu.upeu.mana.dto.auth.LoginRequestDTO;
import pe.edu.upeu.mana.dto.auth.LoginResponseDTO;
import pe.edu.upeu.mana.dto.auth.RecuperarClaveRequestDTO;
import pe.edu.upeu.mana.dto.auth.RestablecerClaveRequestDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);

    LoginResponseDTO buscarActual(String correo);

    void recuperar(RecuperarClaveRequestDTO request);

    void restablecer(RestablecerClaveRequestDTO request);
}
