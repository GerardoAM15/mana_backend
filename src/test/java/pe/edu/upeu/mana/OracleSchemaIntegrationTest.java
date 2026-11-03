package pe.edu.upeu.mana;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.jpa.hibernate.ddl-auto=validate")
@EnabledIfEnvironmentVariable(named = "MANA_ORACLE_TEST", matches = "true")
class OracleSchemaIntegrationTest {
    @Test
    void exportaContratoOpenApi() throws Exception {
        var respuesta=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+puerto+"/v3/api-docs")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,respuesta.statusCode());
        assertTrue(mapper.readTree(respuesta.body()).get("paths").has("/api/v1/ventas"));
        var contrato=mapper.readTree(respuesta.body());
        assertFalse(contrato.get("components").get("schemas").get("LineaVentaRequestDTO").get("properties").has("formaValida"));
        boolean tieneQ=false;
        for(var parametro:contrato.get("paths").get("/api/v1/clientes").get("get").get("parameters")) {
            if(parametro.get("name").asText().equals("q")) {
                tieneQ=true;
                assertFalse(parametro.get("required").asBoolean());
            }
        }
        assertTrue(tieneQ);
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/openapi.json"),respuesta.body());
    }

    @LocalServerPort
    int puerto;

    @Autowired
    ObjectMapper mapper;

    @Value("${app.admin.email}")
    String correo;

    @Value("${app.admin.password}")
    String clave;

    @Test
    void arrancaValidandoElEsquemaReal() {
        // La creación del contexto exige validar todas las tablas con Oracle.
    }

    @Test
    void loginIdentidadYErroresHttp() throws Exception {
        var health=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+puerto+"/api/health")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,health.statusCode());
        var sinToken = enviar("GET", "/auth/me", null, null);
        assertEquals(401, sinToken.statusCode());
        assertEquals(401, mapper.readTree(sinToken.body()).get("status").asInt());
        assertTrue(mapper.readTree(sinToken.body()).has("message"));
        assertTrue(mapper.readTree(sinToken.body()).has("path"));
        assertTrue(mapper.readTree(sinToken.body()).has("validationErrors"));
        assertFalse(mapper.readTree(sinToken.body()).has("mensaje"));
        var invalido = enviar("GET", "/auth/me", null, "invalido");
        assertEquals(401, invalido.statusCode());
        var incorrecto = enviar("POST", "/auth/login", Map.of("correo", correo, "clave", "incorrecta"), null);
        var inexistente = enviar("POST", "/auth/login", Map.of("correo", "no-existe@example.invalid", "clave", "incorrecta"), null);
        assertEquals(401, incorrecto.statusCode());
        assertEquals(401, inexistente.statusCode());
        assertEquals(mapper.readTree(incorrecto.body()).get("message").asText(), mapper.readTree(inexistente.body()).get("message").asText());
        var login = enviar("POST", "/auth/login", Map.of("correo", correo, "clave", clave), null);
        assertEquals(200, login.statusCode());
        var datos = mapper.readTree(login.body());
        assertEquals("Bearer", datos.get("tipo").asText());
        assertTrue(datos.has("permisos"));
        assertFalse(datos.has("claveHash"));
        var me = enviar("GET", "/auth/me", null, datos.get("token").asText());
        assertEquals(200, me.statusCode());
        assertFalse(mapper.readTree(me.body()).has("token"));
        for (String ruta : java.util.List.of("/catalogos", "/clientes", "/usuarios", "/locales", "/platos", "/tarifas", "/planes", "/reglas-cobro", "/precios-sueltos", "/dias-no-atencion", "/menus")) {
            assertEquals(200, enviar("GET", ruta, null, datos.get("token").asText()).statusCode(), ruta);
        }
        var recuperar = enviar("POST", "/auth/recuperar", Map.of("correo", "no-existe@example.invalid"), null);
        assertEquals(200, recuperar.statusCode());
        var malformado = enviar("POST", "/auth/login", Map.of("correo", "mal", "clave", ""), null);
        assertEquals(400, malformado.statusCode());
    }

    private HttpResponse<String> enviar(String metodo, String ruta, Object cuerpo, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/v1" + ruta));
        if (token != null)
            request.header("Authorization", "Bearer " + token);
        request.header("Content-Type", "application/json");
        request.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)));
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
