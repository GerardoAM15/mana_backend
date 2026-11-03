package pe.edu.upeu.mana.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import pe.edu.upeu.mana.exception.dto.ErrorResponseDTO;

@Component
public class RespuestaErrorSeguridad {

    public RespuestaErrorSeguridad(ObjectMapper mapper, Clock clock) {
        this.mapper = mapper;
        this.clock = clock;
    }

    private final ObjectMapper mapper;

    private final Clock clock;

    public void escribir(HttpServletRequest req, HttpServletResponse res, HttpStatus status, String mensaje) throws IOException {
        res.setStatus(status.value());
        res.setCharacterEncoding("UTF-8");
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(mapper.writeValueAsString(new ErrorResponseDTO(LocalDateTime.now(clock), status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI(), null)));
    }
}
