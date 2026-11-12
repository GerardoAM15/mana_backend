package pe.edu.upeu.mana.service;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pe.edu.upeu.mana.entity.Persona;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.repository.PersonaRepository;
import pe.edu.upeu.mana.security.LimitadorIntentos;
import pe.edu.upeu.mana.service.impl.ConsultaDocumentoServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.client.ExpectedCount.once;

@ExtendWith(MockitoExtension.class)
class ConsultaDocumentoTest {

    @Mock PersonaRepository personas;
    Clock clock = Clock.fixed(Instant.parse("2026-11-02T15:00:00Z"), ZoneId.of("America/Lima"));

    MockRestServiceServer servidor;
    ApisPeruCliente cliente;
    ConsultaDocumentoServiceImpl service;

    @BeforeEach
    void preparar() {
        var builder = RestClient.builder().baseUrl("https://api.test/api/v1");
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new ApisPeruCliente(builder.build(), "TOKEN-SECRETO");
        service = new ConsultaDocumentoServiceImpl(personas, cliente, new LimitadorIntentos(clock), clock);
    }

    @Test
    void dniRegistradoSeRespondeDesdeLaBdSinLlamarALaApi() {
        var p = new Persona();
        p.setDni("70000001"); p.setNombre("Lucía"); p.setApellido("Quispe Mamani");
        when(personas.findByDni("70000001")).thenReturn(Optional.of(p));

        var r = service.consultarDni("70000001");

        assertEquals("MANA", r.getFuente());
        assertTrue(r.isRegistrado());
        assertEquals("Quispe Mamani", r.getApellido());
        servidor.verify();
    }

    @Test
    void dniNuevoSeConsultaUnaVezYQuedaEnCache() {
        when(personas.findByDni("12345678")).thenReturn(Optional.empty());
        servidor.expect(once(), requestTo("https://api.test/api/v1/dni/12345678?token=TOKEN-SECRETO"))
                .andRespond(withSuccess("""
                        {"dni":"12345678","nombres":"JUAN CARLOS","apellidoPaterno":"PEREZ","apellidoMaterno":"ROJAS","codVerifica":"1"}
                        """, MediaType.APPLICATION_JSON));

        var r = service.consultarDni("12345678");
        var otra = service.consultarDni("12345678");

        assertEquals("JUAN CARLOS", r.getNombre());
        assertEquals("PEREZ ROJAS", r.getApellido());
        assertEquals("RENIEC", r.getFuente());
        assertFalse(r.isRegistrado());
        assertSame(r, otra);
        servidor.verify();
    }

    @Test
    void dniInexistenteDa404() {
        when(personas.findByDni("12345678")).thenReturn(Optional.empty());
        servidor.expect(requestTo("https://api.test/api/v1/dni/12345678?token=TOKEN-SECRETO"))
                .andRespond(withSuccess("{\"success\":false,\"message\":\"No se encontraron resultados.\"}", MediaType.APPLICATION_JSON));

        assertThrows(RecursoNoEncontradoException.class, () -> service.consultarDni("12345678"));
    }

    @Test
    void dniConFormatoInvalidoNoLlamaALaApi() {
        assertThrows(DatosInvalidosException.class, () -> service.consultarDni("1234"));
        assertThrows(DatosInvalidosException.class, () -> service.consultarDni("1234567a"));
        verifyNoInteractions(personas);
        servidor.verify();
    }

    @Test
    void tokenRechazadoDa503SinExponerElToken() {
        when(personas.findByDni("12345678")).thenReturn(Optional.empty());
        servidor.expect(requestTo("https://api.test/api/v1/dni/12345678?token=TOKEN-SECRETO"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        var ex = assertThrows(ServicioExternoException.class, () -> service.consultarDni("12345678"));
        assertFalse(ex.getMessage().contains("TOKEN"));
    }

    @Test
    void sinTokenConfiguradoDa503() {
        var sinToken = new ConsultaDocumentoServiceImpl(personas, new ApisPeruCliente(RestClient.create(), ""), new LimitadorIntentos(clock), clock);
        when(personas.findByDni("12345678")).thenReturn(Optional.empty());

        assertThrows(ServicioExternoException.class, () -> sinToken.consultarDni("12345678"));
    }

    @Test
    void rucDevuelveRazonSocialYPrimerTelefono() {
        servidor.expect(requestTo("https://api.test/api/v1/ruc/20123456789?token=TOKEN-SECRETO"))
                .andRespond(withSuccess("""
                        {"ruc":"20123456789","razonSocial":"DISTRIBUIDORA LIMA S.A.C.","direccion":"AV. LIMA 123",
                         "telefonos":["","014567890"],"estado":"ACTIVO","condicion":"HABIDO","ubigeo":"150101"}
                        """, MediaType.APPLICATION_JSON));

        var r = service.consultarRuc("20123456789");

        assertEquals("DISTRIBUIDORA LIMA S.A.C.", r.getRazonSocial());
        assertEquals("014567890", r.getTelefono());
        assertEquals("ACTIVO", r.getEstado());
        assertEquals("HABIDO", r.getCondicion());
    }

    @Test
    void rucConPrefijoInvalidoDa400() {
        assertThrows(DatosInvalidosException.class, () -> service.consultarRuc("30123456789"));
        assertThrows(DatosInvalidosException.class, () -> service.consultarRuc("2012345678"));
        servidor.verify();
    }

    @Test
    void masDe30ConsultasPorMinutoDa429() {
        when(personas.findByDni(anyString())).thenReturn(Optional.empty());
        for (int i = 0; i < 30; i++) {
            String dni = "1000%04d".formatted(i);
            servidor.expect(requestTo("https://api.test/api/v1/dni/" + dni + "?token=TOKEN-SECRETO"))
                    .andRespond(withSuccess("{\"dni\":\"" + dni + "\",\"nombres\":\"A\",\"apellidoPaterno\":\"B\",\"apellidoMaterno\":\"C\"}", MediaType.APPLICATION_JSON));
        }
        for (int i = 0; i < 30; i++)
            service.consultarDni("1000%04d".formatted(i));
        // La consulta 31 del mismo minuto se rechaza sin llegar a la API.
        assertThrows(DemasiadosIntentosException.class, () -> service.consultarDni("20009999"));
        servidor.verify();
    }
}
