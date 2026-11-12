package pe.edu.upeu.mana.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.exception.ServicioExternoException;

@Component
public class ApisPeruFacturacionCliente {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApisPeruFacturacionCliente.class);
    private final RestClient http;
    private final FacturacionProperties config;
    @Autowired
    public ApisPeruFacturacionCliente(FacturacionProperties config) {
        this(crear(config), config);
    }
    public ApisPeruFacturacionCliente(RestClient http, FacturacionProperties config) { this.http=http; this.config=config; }
    private static RestClient crear(FacturacionProperties config) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(15));
        return RestClient.builder().baseUrl(config.getUrl()).requestFactory(factory).build();
    }
    public JsonNode empresa() {
        validarToken();
        if (config.getCompanyId() == null) throw new ServicioExternoException("Configure el company-id de la empresa en APIsPERU");
        try { return http.get().uri("/companies/{id}", config.getCompanyId()).headers(h -> h.setBearerAuth(config.getToken())).retrieve().body(JsonNode.class); }
        catch (RestClientException ex) { throw fallo(ex); }
    }
    public JsonNode enviar(boolean nota, Map<String,Object> payload) {
        validarToken();
        try { return http.post().uri(nota ? "/note/send" : "/invoice/send").headers(h -> h.setBearerAuth(config.getToken()))
            .contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().body(JsonNode.class); }
        catch (RestClientException ex) { throw fallo(ex); }
    }
    // GET /invoice/status: CDR de una factura (01) o boleta (03) ya registrada en SUNAT.
    public JsonNode estado(String tipoSunat, String serie, Integer numero) {
        validarToken();
        try { return http.get().uri(u -> u.path("/invoice/status").queryParam("tipo", tipoSunat).queryParam("serie", serie)
                .queryParam("numero", String.valueOf(numero)).queryParam("ruc", config.getEmpresa().getRuc()).build())
            .headers(h -> h.setBearerAuth(config.getToken())).retrieve().body(JsonNode.class); }
        catch (RestClientException ex) { throw fallo(ex); }
    }
    public byte[] pdf(boolean nota, Map<String,Object> payload) {
        validarToken();
        try { return http.post().uri(nota ? "/note/pdf" : "/invoice/pdf").headers(h -> h.setBearerAuth(config.getToken()))
            .contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().body(byte[].class); }
        catch (RestClientException ex) { throw fallo(ex); }
    }
    private void validarToken() { if (config.getToken() == null || config.getToken().isBlank()) throw new ServicioExternoException("Configure el token de facturación de APIsPERU"); }
    private ServicioExternoException fallo(RestClientException ex) {
        // No registrar URL, cabeceras ni el mensaje de la excepción (pueden contener el token).
        if (ex instanceof HttpStatusCodeException h) {
            int estado = h.getStatusCode().value();
            log.warn("APIsPERU facturación respondió {}", estado);
            if (estado == 401 || estado == 403)
                return new ServicioExternoException("APIsPERU rechazó el token de facturación (" + estado + "); revise app.facturacion.token");
            if (h.getStatusCode().is4xxClientError())
                return new ServicioExternoException("APIsPERU rechazó los datos del comprobante (" + estado + "): " + detalle(h.getResponseBodyAsString()));
        } else {
            log.warn("APIsPERU facturación no disponible | error={}", ex.getClass().getSimpleName());
        }
        return new ServicioExternoException("La API de facturación no está disponible; se reintentará el envío");
    }

    // Mensaje de validación de APIsPERU: {"error": "..."} o {"message": "..."}.
    static String detalle(String cuerpo) {
        try {
            JsonNode n = LECTOR.readTree(cuerpo);
            for (String campo : new String[] {"error", "message"}) {
                JsonNode v = n.path(campo);
                if (v.isTextual() && !v.asText().isBlank()) return recortar(v.asText());
                if (v.isObject() || v.isArray()) return recortar(v.toString());
            }
        } catch (RuntimeException ignorado) {
            // Cuerpo no JSON.
        }
        return cuerpo == null || cuerpo.isBlank() ? "sin detalle" : recortar(cuerpo);
    }
    private static String recortar(String s) { return s.length() > 300 ? s.substring(0, 300) : s; }
    private static final tools.jackson.databind.ObjectMapper LECTOR = new tools.jackson.databind.ObjectMapper();
}
