package pe.edu.upeu.mana.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public RestAuthenticationEntryPoint(RespuestaErrorSeguridad errores) {
        this.errores = errores;
    }

    private final RespuestaErrorSeguridad errores;

    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        errores.escribir(req, res, HttpStatus.UNAUTHORIZED, "Token ausente, invalido o vencido");
    }
}
