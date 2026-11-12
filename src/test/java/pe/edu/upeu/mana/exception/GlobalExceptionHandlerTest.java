package pe.edu.upeu.mana.exception;

import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void efectivoInsuficienteResponde409ConElMensajeSolicitado() {
        var handler = new GlobalExceptionHandler(Clock.fixed(Instant.parse("2026-11-03T15:00:00Z"), ZoneId.of("America/Lima")));
        var respuesta = handler.conflicto(new ReglaNegocioException("No hay efectivo suficiente en caja para la devolución"), new MockHttpServletRequest("PATCH", "/api/v1/contratos/1/anular"));
        assertEquals(409, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
        assertEquals("No hay efectivo suficiente en caja para la devolución", respuesta.getBody().getMessage());
        assertEquals("/api/v1/contratos/1/anular", respuesta.getBody().getPath());
    }
}
