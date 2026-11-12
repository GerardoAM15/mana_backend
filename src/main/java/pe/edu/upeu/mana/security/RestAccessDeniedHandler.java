package pe.edu.upeu.mana.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    public RestAccessDeniedHandler(RespuestaErrorSeguridad errores) {
        this.errores = errores;
    }

    private final RespuestaErrorSeguridad errores;

    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        errores.escribir(req, res, HttpStatus.FORBIDDEN, "No tiene permiso para esta operacion");
    }
}
