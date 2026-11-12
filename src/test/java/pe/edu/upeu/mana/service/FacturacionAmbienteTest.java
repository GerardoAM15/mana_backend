package pe.edu.upeu.mana.service;
import java.time.*;
import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.enums.AmbienteFacturacion;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class FacturacionAmbienteTest {
    ObjectMapper json=new ObjectMapper();
    LectorAmbienteEmpresa lector=new LectorAmbienteEmpresa();
    @Test void textoBeta() { assertEquals("beta",lector.leer(json.readTree("\"beta\"")).orElseThrow()); }
    @Test void objetoRecursivoSinNombresSupuestos() { assertEquals("beta",lector.leer(json.readTree("{\"cualquier\":{\"otro\":[\"beta\",\"beta\"]}}")).orElseThrow()); }
    @Test void objetoSinConocidos() { assertTrue(lector.leer(json.readTree("{\"id\":1,\"valor\":\"desconocido\"}")).isEmpty()); }
    @Test void objetoAmbiguo() { assertTrue(lector.leer(json.readTree("{\"uno\":\"beta\",\"dos\":\"produccion\"}")).isEmpty()); }
    @Test void formatoRealDeApisPeru() {
        assertEquals("beta",lector.leer(json.readTree("{\"nombre\":\"beta\",\"fe_url\":\"https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService\",\"auth_url\":\"https://gre-test.nubefact.com/v1\"}")).orElseThrow());
        assertTrue(lector.leer(json.readTree("{\"nombre\":\"otro\",\"x\":\"beta\"}")).isEmpty());
    }
    @Test void noNormalizaValoresDesconocidos() { assertTrue(lector.leer(json.readTree("\"BETA\"")).isEmpty()); }
    static class Reloj extends Clock {
        Instant ahora=Instant.parse("2026-11-10T15:00:00Z");
        public ZoneId getZone(){ return ZoneId.of("America/Lima"); }
        public Clock withZone(ZoneId z){ return Clock.fixed(ahora,z); }
        public Instant instant(){ return ahora; }
    }
    FacturacionProperties config;
    MockRestServiceServer server;
    VerificadorAmbienteFacturacion verificador;
    Reloj reloj;
    @BeforeEach void preparar() {
        config=new FacturacionProperties(); config.setHabilitada(true); config.setCompanyId(42L); config.setToken("token-prueba");
        var b=RestClient.builder().baseUrl("https://facturacion.test/api/v1"); server=MockRestServiceServer.bindTo(b).build(); reloj=new Reloj();
        verificador=new VerificadorAmbienteFacturacion(new ApisPeruFacturacionCliente(b.build(),config),lector,config,reloj);
    }
    void empresa(String environment) {
        server.expect(requestTo("https://facturacion.test/api/v1/companies/42")).andExpect(header("Authorization","Bearer token-prueba"))
            .andRespond(withSuccess("{\"environment\":"+environment+"}",MediaType.APPLICATION_JSON));
    }
    @Test void coincideYCacheDuraDiezMinutos() {
        empresa("\"beta\""); empresa("\"produccion\"");
        assertEquals(VerificadorAmbienteFacturacion.Estado.COINCIDE,verificador.verificar().estado());
        reloj.ahora=reloj.ahora.plusSeconds(599);
        assertEquals(VerificadorAmbienteFacturacion.Estado.COINCIDE,verificador.verificar().estado());
        reloj.ahora=reloj.ahora.plusSeconds(1);
        assertEquals(VerificadorAmbienteFacturacion.Estado.DISCREPANCIA,verificador.verificar().estado()); server.verify();
    }
    @Test void noCoincideMensajeExacto() {
        empresa("{\"sinDependerDelNombre\":\"produccion\"}");
        var r=verificador.verificar(); assertEquals(VerificadorAmbienteFacturacion.Estado.DISCREPANCIA,r.estado());
        assertEquals("El ambiente configurado en MANÁ (BETA) no coincide con el de la empresa en APIsPERU (produccion)",r.mensaje()); server.verify();
    }
    @Test void caidaEsTemporal() {
        server.expect(requestTo("https://facturacion.test/api/v1/companies/42")).andRespond(withServerError());
        assertEquals(VerificadorAmbienteFacturacion.Estado.TEMPORAL,verificador.verificar().estado()); server.verify();
    }
    @Test void noDeterminableEsTemporal() { empresa("{\"id\":1}"); assertEquals(VerificadorAmbienteFacturacion.Estado.TEMPORAL,verificador.verificar().estado()); server.verify(); }
    @Test void admiteNubefactBeta() { empresa("\"nubefact_beta\""); assertEquals(VerificadorAmbienteFacturacion.Estado.COINCIDE,verificador.verificar().estado()); server.verify(); }
    @Test void admiteNubefactProduccion() { config.setAmbiente(AmbienteFacturacion.PRODUCCION); empresa("\"nubefact_produccion\""); assertEquals(VerificadorAmbienteFacturacion.Estado.COINCIDE,verificador.verificar().estado()); server.verify(); }
}
