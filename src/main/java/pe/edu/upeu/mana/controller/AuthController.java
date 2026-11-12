package pe.edu.upeu.mana.controller;

import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.AuthService;
import pe.edu.upeu.mana.dto.auth.LoginRequestDTO;
import pe.edu.upeu.mana.dto.auth.LoginResponseDTO;
import pe.edu.upeu.mana.dto.comun.MensajeResponseDTO;
import pe.edu.upeu.mana.dto.auth.RecuperarClaveRequestDTO;
import pe.edu.upeu.mana.dto.auth.RestablecerClaveRequestDTO;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO r) {
        return service.login(r);
    }

    @GetMapping("/me")
    public LoginResponseDTO buscarActual(Principal p) {
        return service.buscarActual(p.getName());
    }

    @PostMapping("/recuperar")
    public MensajeResponseDTO recuperar(@Valid @RequestBody RecuperarClaveRequestDTO r) {
        service.recuperar(r);
        return new MensajeResponseDTO("Si el correo está registrado, recibirá un enlace de recuperación");
    }

    @PostMapping("/restablecer")
    public MensajeResponseDTO restablecer(@Valid @RequestBody RestablecerClaveRequestDTO r) {
        service.restablecer(r);
        return new MensajeResponseDTO("Contraseña actualizada");
    }
}
