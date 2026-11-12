package pe.edu.upeu.mana.security;

import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

class RestAccessDeniedHandlerTest {
    @Test void prohibidoUsaElContratoDeErrorPharmaSoft() throws Exception {
        var mapper=JsonMapper.builder().findAndAddModules().build();
        var errores=new RespuestaErrorSeguridad(mapper,Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"),ZoneId.of("America/Lima")));
        var request=new MockHttpServletRequest("GET","/api/v1/usuarios");
        var response=new MockHttpServletResponse();
        new RestAccessDeniedHandler(errores).handle(request,response,new AccessDeniedException("Sin permiso"));
        var json=mapper.readTree(response.getContentAsString());
        assertEquals(403,response.getStatus());
        assertEquals(403,json.get("status").asInt());
        assertEquals("/api/v1/usuarios",json.get("path").asText());
        assertTrue(json.has("message"));assertTrue(json.has("validationErrors"));assertFalse(json.has("mensaje"));
    }
}
