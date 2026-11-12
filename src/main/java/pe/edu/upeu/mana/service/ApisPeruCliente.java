package pe.edu.upeu.mana.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.edu.upeu.mana.exception.ServicioExternoException;

// Cliente HTTP de APIsPERU (consulta DNI/RUC). El token viaja en la URL
// (así lo exige la API): nunca se registra la URL ni el mensaje de las
// excepciones, que la incluyen.
@Component
public class ApisPeruCliente {

    private static final Logger log = LoggerFactory.getLogger(ApisPeruCliente.class);

    private static final String NO_DISPONIBLE = "La consulta de DNI/RUC no está disponible; ingrese los datos manualmente";

    private final RestClient http;

    private final String token;

    @Autowired
    public ApisPeruCliente(@Value("${app.apisperu.url:https://dniruc.apisperu.com/api/v1}") String url,
                           @Value("${app.apisperu.token:}") String token) {
        this(RestClient.builder().baseUrl(url).requestFactory(fabrica()).build(), token);
    }

    // Para pruebas: permite inyectar un RestClient enlazado a un servidor simulado.
    public ApisPeruCliente(RestClient http, String token) {
        this.http = http;
        this.token = token == null ? "" : token.trim();
    }

    private static JdkClientHttpRequestFactory fabrica() {
        var cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        var fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        return fabrica;
    }

    public boolean configurado() {
        return !token.isBlank();
    }

    public Optional<PersonaReniec> dni(String numero) {
        var r = consultar("/dni/{numero}", numero, PersonaReniec.class);
        return r.filter(p -> p.nombres() != null && !p.nombres().isBlank());
    }

    public Optional<EmpresaSunat> ruc(String numero) {
        var r = consultar("/ruc/{numero}", numero, EmpresaSunat.class);
        return r.filter(e -> e.razonSocial() != null && !e.razonSocial().isBlank());
    }

    private <T> Optional<T> consultar(String ruta, String numero, Class<T> tipo) {
        if (!configurado())
            throw new ServicioExternoException(NO_DISPONIBLE);
        try {
            return Optional.ofNullable(http.get()
                    .uri(b -> b.path(ruta).queryParam("token", token).build(numero))
                    .retrieve()
                    .body(tipo));
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.UnprocessableContent ex) {
            return Optional.empty();
        } catch (HttpClientErrorException ex) {
            // 401/403 = token inválido o vencido; 429 = cuota agotada.
            log.warn("APIsPERU rechazó la consulta | estado={}", ex.getStatusCode().value());
            throw new ServicioExternoException(NO_DISPONIBLE);
        } catch (RestClientException ex) {
            // Sin conexión, tiempo agotado, 5xx o respuesta ilegible. No se registra ex.getMessage() (contiene la URL con el token).
            log.warn("APIsPERU no respondió | error={}", ex.getClass().getSimpleName());
            throw new ServicioExternoException(NO_DISPONIBLE);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PersonaReniec(String dni, String nombres, String apellidoPaterno, String apellidoMaterno) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmpresaSunat(String ruc, String razonSocial, String direccion, List<String> telefonos,
                               String estado, String condicion) {
    }
}
