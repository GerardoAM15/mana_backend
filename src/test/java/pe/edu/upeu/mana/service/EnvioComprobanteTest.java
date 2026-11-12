package pe.edu.upeu.mana.service;
import pe.edu.upeu.mana.service.service.EnvioComprobanteService;
import pe.edu.upeu.mana.service.impl.EnvioComprobanteServiceImpl;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.mapper.ComprobantePayloadMapper;
import pe.edu.upeu.mana.repository.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class EnvioComprobanteTest {
    ComprobanteRepository comprobantes;
    ComprobanteDetalleRepository detalles;
    FacturacionProperties config;
    MockRestServiceServer server;
    EnvioComprobanteService envio;
    Comprobante c;
    Clock clock=Clock.fixed(Instant.parse("2026-11-10T15:00:00Z"),ZoneId.of("America/Lima"));
    @BeforeEach void preparar() {
        comprobantes=mock(ComprobanteRepository.class); detalles=mock(ComprobanteDetalleRepository.class);
        config=new FacturacionProperties(); config.setHabilitada(true); config.setToken("token-prueba"); config.setCompanyId(42L);
        config.getEmpresa().setRuc("20123456789"); config.getEmpresa().setRazonSocial("MANÁ");
        var b=RestClient.builder().baseUrl("https://facturacion.test/api/v1"); server=MockRestServiceServer.bindTo(b).build();
        var cliente=new ApisPeruFacturacionCliente(b.build(),config);
        var verificador=new VerificadorAmbienteFacturacion(cliente,new LectorAmbienteEmpresa(),config,clock);
        envio=new EnvioComprobanteServiceImpl(comprobantes,detalles,cliente,verificador,new ComprobantePayloadMapper(config,clock),config,clock);
        c=new Comprobante(); c.setId(1L); c.setVentaId(3L); c.setSerie("B001"); c.setNumero(1); c.setTipo(TipoComprobante.BOLETA);
        c.setEstado(EstadoComprobante.PENDIENTE); c.setAmbiente(AmbienteFacturacion.BETA); c.setFechaEmision(LocalDateTime.now(clock));
        c.setReceptorTipoDoc("0"); c.setReceptorNombre("CLIENTES VARIOS"); c.setPorcentajeIgv(new BigDecimal("18.00"));
        c.setOpGravadas(new BigDecimal("100.00")); c.setIgv(new BigDecimal("18.00")); c.setTotal(new BigDecimal("118.00"));
        when(comprobantes.bloquear(1L)).thenReturn(Optional.of(c));
    }
    void ambiente(String json) { server.expect(requestTo("https://facturacion.test/api/v1/companies/42")).andRespond(withSuccess("{\"environment\":"+json+"}",MediaType.APPLICATION_JSON)); }
    @ParameterizedTest @ValueSource(strings={"ACEPTADO","OBSERVADO","RECHAZADO","CAIDA","TIMEOUT"})
    void enviaConRespuestaSunatOCaida(String resultado) {
        ambiente("\"beta\"");
        var req=server.expect(requestTo("https://facturacion.test/api/v1/invoice/send")).andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization","Bearer token-prueba")).andExpect(jsonPath("$.tipoDoc").value("03"))
            .andExpect(jsonPath("$.serie").value("B001")).andExpect(jsonPath("$.correlativo").value("1"));
        if(resultado.equals("CAIDA")) req.andRespond(withServerError());
        else if(resultado.equals("TIMEOUT")) req.andRespond(withException(new java.net.SocketTimeoutException("simulado")));
        else req.andRespond(withSuccess("{\"xml\":\"<xml/>\",\"hash\":\"hash\",\"sunatResponse\":{\"success\":true,\"cdrZip\":\"Y2Ry\",\"cdrResponse\":{\"accepted\":"+!resultado.equals("RECHAZADO")+",\"code\":\""+(resultado.equals("RECHAZADO")?"2001":"0")+"\",\"description\":\"Respuesta SUNAT\",\"notes\":"+(resultado.equals("OBSERVADO")?"[\"Observación\"]":"[]")+"}}}",MediaType.APPLICATION_JSON));
        envio.enviar(1L);
        assertEquals(resultado.equals("CAIDA")||resultado.equals("TIMEOUT")?EstadoComprobante.ERROR:EstadoComprobante.valueOf(resultado),c.getEstado());
        assertEquals(1,c.getIntentos()); assertNotNull(c.getUltimoIntentoEn());
        if(c.getEstado()!=EstadoComprobante.ERROR) { assertEquals("<xml/>",c.getXml()); assertEquals("Y2Ry",c.getCdr()); }
        server.verify();
    }
    // Respuesta real de APIsPERU: el CDR no trae "accepted", solo id, code, description y notes.
    @ParameterizedTest @org.junit.jupiter.params.provider.CsvSource({"0,[],ACEPTADO", "0,[\"4096 - provincia\"],OBSERVADO", "4096,[],ACEPTADO", "2017,[],RECHAZADO"})
    void cdrSinAcceptedSeDecidePorCodigo(String codigo, String notas, EstadoComprobante esperado) {
        ambiente("\"beta\"");
        server.expect(requestTo("https://facturacion.test/api/v1/invoice/send")).andRespond(withSuccess(
            "{\"sunatResponse\":{\"success\":true,\"cdrZip\":\"Q0RS\",\"cdrResponse\":{\"id\":\"B001-1\",\"code\":\""+codigo+"\",\"description\":\"La Boleta numero B001-1, ha sido aceptada\",\"notes\":"+notas+"}}}",
            MediaType.APPLICATION_JSON));
        envio.enviar(1L); assertEquals(esperado,c.getEstado()); server.verify();
    }
    @Test void enviaDepartamentoProvinciaYDistritoDelEmisor() {
        var e=config.getEmpresa(); e.setDepartamento("LIMA"); e.setProvincia("LIMA"); e.setDistrito("LURIGANCHO"); e.setUbigeo("150118");
        ambiente("\"beta\"");
        server.expect(requestTo("https://facturacion.test/api/v1/invoice/send"))
            .andExpect(jsonPath("$.company.address.departamento").value("LIMA")).andExpect(jsonPath("$.company.address.provincia").value("LIMA"))
            .andExpect(jsonPath("$.company.address.distrito").value("LURIGANCHO")).andExpect(jsonPath("$.company.address.codLocal").value("0000"))
            .andRespond(withSuccess("{\"sunatResponse\":{\"cdrResponse\":{\"code\":\"0\",\"notes\":[]}}}",MediaType.APPLICATION_JSON));
        envio.enviar(1L); assertEquals(EstadoComprobante.ACEPTADO,c.getEstado()); server.verify();
    }
    @Test void fechaSinFraccionNiSegundosOmitidos() {
        // LocalDateTime.now trae nanosegundos y OffsetDateTime.toString omite ":00": APIsPERU responde 400 a ambos.
        c.setFechaEmision(LocalDateTime.of(2026,11,10,10,0,0,123_456_789));
        ambiente("\"beta\"");
        server.expect(requestTo("https://facturacion.test/api/v1/invoice/send")).andExpect(jsonPath("$.fechaEmision").value("2026-11-10T10:00:00-05:00"))
            .andRespond(withSuccess("{\"sunatResponse\":{\"cdrResponse\":{\"accepted\":true,\"code\":\"0\",\"notes\":[]}}}",MediaType.APPLICATION_JSON));
        envio.enviar(1L); assertEquals(EstadoComprobante.ACEPTADO,c.getEstado()); server.verify();
    }
    @Test void error400MuestraElMensajeDeApisPeru() {
        ambiente("\"beta\"");
        server.expect(requestTo("https://facturacion.test/api/v1/invoice/send")).andRespond(withBadRequest()
            .body("{\"error\":\"La fecha enviada no incluye el huso horario\"}").contentType(MediaType.APPLICATION_JSON));
        envio.enviar(1L);
        assertEquals(EstadoComprobante.ERROR,c.getEstado());
        assertTrue(c.getSunatMensaje().contains("(400): La fecha enviada no incluye el huso horario"),c.getSunatMensaje());
        server.verify();
    }
    @Test void tokenRechazadoLoIndica() {
        ambiente("\"beta\"");
        server.expect(requestTo("https://facturacion.test/api/v1/invoice/send")).andRespond(withUnauthorizedRequest());
        envio.enviar(1L); assertTrue(c.getSunatMensaje().contains("token")); server.verify();
    }
    @Test void discrepanciaNoEnviaNiConsumeIntentos() {
        ambiente("\"produccion\""); envio.enviar(1L); assertEquals(EstadoComprobante.PENDIENTE,c.getEstado()); assertEquals(0,c.getIntentos());
        assertTrue(c.getSunatMensaje().contains("no coincide")); server.verify();
    }
    @Test void ambienteNoDeterminableDejaPendiente() {
        ambiente("{}"); envio.enviar(1L); assertEquals(EstadoComprobante.PENDIENTE,c.getEstado()); assertEquals(0,c.getIntentos()); server.verify();
    }
    @Test void consultaDeAmbienteCaidaDejaPendiente() {
        server.expect(requestTo("https://facturacion.test/api/v1/companies/42")).andRespond(withServerError());
        envio.enviar(1L); assertEquals(EstadoComprobante.PENDIENTE,c.getEstado()); assertEquals(0,c.getIntentos()); server.verify();
    }
    @Test void cincoIntentosNoVuelveAEnviar() { c.setIntentos(5); envio.enviar(1L); server.verify(); }
    @Test void deshabilitadaNoConsultaNada() { config.setHabilitada(false); envio.enviar(1L); verifyNoInteractions(comprobantes); server.verify(); }
    void respuestaSinCdr(String codigo) {
        server.expect(requestTo("https://facturacion.test/api/v1/invoice/send")).andRespond(withSuccess(
            "{\"xml\":\"<xml/>\",\"hash\":\"hash\",\"sunatResponse\":{\"success\":false,\"error\":{\"code\":\""+codigo+"\",\"message\":\"Mensaje SUNAT\"}}}",MediaType.APPLICATION_JSON));
    }
    static final String STATUS="https://facturacion.test/api/v1/invoice/status?tipo=03&serie=B001&numero=1&ruc=20123456789";
    @ParameterizedTest @ValueSource(strings={"2017","2800","3999"})
    void codigoDeRechazoSinCdrQuedaRechazado(String codigo) {
        ambiente("\"beta\""); respuestaSinCdr(codigo); envio.enviar(1L);
        assertEquals(EstadoComprobante.RECHAZADO,c.getEstado()); assertEquals(codigo,c.getSunatCodigo()); assertEquals("Mensaje SUNAT",c.getSunatMensaje());
        server.verify();
    }
    @ParameterizedTest @ValueSource(strings={"0109","0200","1999","4000",""})
    void excepcionSinCdrQuedaErrorParaReintentar(String codigo) {
        ambiente("\"beta\""); respuestaSinCdr(codigo); envio.enviar(1L);
        assertEquals(EstadoComprobante.ERROR,c.getEstado()); assertEquals(1,c.getIntentos()); server.verify();
    }
    @Test void yaRegistradoConsultaElCdrYLoAplica() {
        ambiente("\"beta\""); respuestaSinCdr("1033");
        server.expect(requestTo(STATUS)).andExpect(method(HttpMethod.GET)).andExpect(header("Authorization","Bearer token-prueba"))
            .andRespond(withSuccess("{\"success\":true,\"cdrZip\":\"Q0RS\",\"cdrResponse\":{\"accepted\":true,\"code\":\"0\",\"description\":\"Aceptada\",\"notes\":[]}}",MediaType.APPLICATION_JSON));
        envio.enviar(1L);
        assertEquals(EstadoComprobante.ACEPTADO,c.getEstado()); assertEquals("Q0RS",c.getCdr()); assertEquals("0",c.getSunatCodigo()); server.verify();
    }
    @Test void yaRegistradoSinCdrDejaDeReintentar() {
        ambiente("\"beta\""); respuestaSinCdr("1033");
        server.expect(requestTo(STATUS)).andRespond(withSuccess("{\"success\":false,\"error\":{\"code\":\"0127\",\"message\":\"Sin CDR\"}}",MediaType.APPLICATION_JSON));
        envio.enviar(1L);
        assertEquals(EstadoComprobante.ERROR,c.getEstado()); assertEquals(5,c.getIntentos()); assertTrue(c.getSunatMensaje().contains("1033"));
        server.verify();
    }
    @Test void yaRegistradoEnNotaNoConsultaEstadoYDejaDeReintentar() {
        ambiente("\"beta\""); var ref=new Comprobante(); ref.setTipo(TipoComprobante.BOLETA); ref.setSerie("B001"); ref.setNumero(7);
        c.setTipo(TipoComprobante.NOTA_CREDITO); c.setSerie("BC01"); c.setReferenciaId(9L); c.setVentaId(null); c.setMotivoCodigo("01"); c.setMotivoDescripcion("Anulación de la operación");
        when(comprobantes.findById(9L)).thenReturn(Optional.of(ref));
        server.expect(requestTo("https://facturacion.test/api/v1/note/send")).andRespond(withSuccess(
            "{\"sunatResponse\":{\"success\":false,\"error\":{\"code\":\"1033\",\"message\":\"Registrado previamente\"}}}",MediaType.APPLICATION_JSON));
        envio.enviar(1L);
        assertEquals(EstadoComprobante.ERROR,c.getEstado()); assertEquals(5,c.getIntentos()); server.verify();
    }
    @Test void notaUsaEndpointYReferenciaOficiales() {
        ambiente("\"beta\""); var ref=new Comprobante(); ref.setTipo(TipoComprobante.BOLETA); ref.setSerie("B001"); ref.setNumero(7);
        c.setTipo(TipoComprobante.NOTA_CREDITO); c.setSerie("BC01"); c.setReferenciaId(9L); c.setVentaId(null); c.setMotivoCodigo("01"); c.setMotivoDescripcion("Anulación de la operación");
        when(comprobantes.findById(9L)).thenReturn(Optional.of(ref));
        server.expect(requestTo("https://facturacion.test/api/v1/note/send")).andExpect(jsonPath("$.tipoDoc").value("07"))
            .andExpect(jsonPath("$.numDocfectado").value("B001-7")).andExpect(jsonPath("$.tipDocAfectado").value("03"))
            .andExpect(jsonPath("$.codMotivo").value("01"))
            .andRespond(withSuccess("{\"sunatResponse\":{\"cdrResponse\":{\"accepted\":true,\"code\":\"0\",\"notes\":[]}}}",MediaType.APPLICATION_JSON));
        envio.enviar(1L); assertEquals(EstadoComprobante.ACEPTADO,c.getEstado()); server.verify();
    }
}
