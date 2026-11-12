package pe.edu.upeu.mana;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.test.util.ReflectionTestUtils;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.dto.comprobante.ComprobanteRequestDTO;
import pe.edu.upeu.mana.dto.venta.PagosVentaRequestDTO;
import pe.edu.upeu.mana.dto.comun.*;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.service.service.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(properties="spring.jpa.hibernate.ddl-auto=validate")
@EnabledIfEnvironmentVariable(named="MANA_ORACLE_TEST",matches="true")
class ComprobanteCobroOracleTest {
    @Autowired PlatformTransactionManager manager;
    @Autowired JdbcTemplate jdbc;
    @Autowired LocalRepository locales;
    @Autowired EmpleadoRepository empleados;
    @Autowired UsuarioRepository usuarios;
    @Autowired SesionCajaRepository cajas;
    @Autowired BebidaRepository bebidas;
    @Autowired StockBebidaRepository stocks;
    @Autowired VentaRepository ventas;
    @Autowired DetalleOrdenRepository detalles;
    @Autowired SerieComprobanteRepository series;
    @Autowired ComprobanteRepository comprobantes;
    @Autowired VentaService ventaService;
    @Autowired FacturacionProperties config;
    @Autowired VerificadorAmbienteFacturacion verificador;
    @Autowired Clock clock;
    @MockitoBean ApisPeruFacturacionCliente cliente;
    Long localId,ventaId,empleadoId,bebidaId;
    FacturacionProperties.Empresa emisorAnterior;
    MockRestServiceServer server;
    String serieBoleta,serieNota;
    @BeforeEach void preparar() {
        var b=RestClient.builder().baseUrl("https://facturacion.test/api/v1"); server=MockRestServiceServer.bindTo(b).build();
        var remoto=new ApisPeruFacturacionCliente(b.build(),config);
        when(cliente.empresa()).thenAnswer(a -> remoto.empresa());
        when(cliente.enviar(anyBoolean(),any())).thenAnswer(a -> remoto.enviar(a.getArgument(0),a.getArgument(1)));
        config.setToken("prueba-no-real"); config.setCompanyId(42L);
        emisorAnterior=config.getEmpresa(); var emisor=new FacturacionProperties.Empresa(); emisor.setRuc("20123456789"); emisor.setRazonSocial("MANÁ PRUEBA"); emisor.setDireccion("Prueba"); emisor.setUbigeo("150101"); config.setEmpresa(emisor);
        ReflectionTestUtils.setField(verificador,"vence",Instant.MIN);
        new TransactionTemplate(manager).executeWithoutResult(status -> {
            var local=new Local(); local.setNombre("Prueba cobro electrónico "+UUID.randomUUID()); locales.saveAndFlush(local); localId=local.getId();
            var e=new Empleado(); e.setLocalId(localId); e.setDni(String.format("%08d",10000000+new Random().nextInt(80000000))); e.setNombre("Prueba"); e.setApellido("Electrónica"); e.setCargo("Cajero"); empleados.saveAndFlush(e); empleadoId=e.getId();
            var u=new Usuario(); u.setEmpleadoId(e.getId()); u.setCorreo("facturacion-"+UUID.randomUUID()+"@example.invalid"); u.setClaveHash("prueba"); u.setRol(RolUsuario.CAJERO); usuarios.saveAndFlush(u);
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u.getCorreo(),null,List.of()));
            var caja=new SesionCaja(); caja.setLocalId(localId); caja.setUsuarioId(u.getId()); caja.setClaveAbierta(u.getId().toString()); caja.setApertura(LocalDateTime.now(clock)); caja.setMontoInicial(new BigDecimal("0.00")); cajas.saveAndFlush(caja);
            var bebida=new Bebida(); bebida.setNombre("Prueba electrónica "+UUID.randomUUID()); bebida.setPrecioVenta(new BigDecimal("118.00")); bebidas.saveAndFlush(bebida); bebidaId=bebida.getId();
            var stock=new StockBebida(); stock.setLocalId(localId); stock.setBebidaId(bebidaId); stock.setStock(5L); stocks.saveAndFlush(stock);
            var v=new Venta(); v.setLocalId(localId); v.setCajaId(caja.getId()); v.setServicio("A"); v.setNumero("V-000001"); v.setParaLlevar(false); v.setEstado(EstadoVenta.PENDIENTE); v.setFecha(LocalDateTime.now(clock)); v.setRecargoLlevar(new BigDecimal("0.00")); v.setTotal(new BigDecimal("118.00")); ventas.saveAndFlush(v); ventaId=v.getId();
            var d=new DetalleOrden(); d.setVentaId(ventaId); d.setTipo(TipoLineaVenta.BEBIDA); d.setBebidaId(bebidaId); d.setDescripcion("Prueba electrónica"); d.setCantidad(1L); d.setPrecioUnitario(new BigDecimal("118.00")); d.setRecargoUnitario(new BigDecimal("0.00")); detalles.saveAndFlush(d);
            var usadas=series.findAll().stream().map(SerieComprobante::getSerie).toList();
            serieBoleta=java.util.stream.IntStream.rangeClosed(1,999).mapToObj(n -> "B%03d".formatted(n)).filter(n -> !usadas.contains(n)).findFirst().orElseThrow();
            serieNota=java.util.stream.IntStream.rangeClosed(1,999).mapToObj(n -> "F%03d".formatted(n)).filter(n -> !usadas.contains(n)).findFirst().orElseThrow();
            for(var tipo:List.of(TipoSerie.BOLETA,TipoSerie.NOTA_CREDITO_BOLETA)) {
                var s=new SerieComprobante(); s.setLocalId(localId); s.setTipo(tipo); s.setSerie(tipo==TipoSerie.BOLETA?serieBoleta:"BC"+serieBoleta.substring(2));
                if(tipo==TipoSerie.NOTA_CREDITO_BOLETA && usadas.contains(s.getSerie())) {
                    s.setSerie(java.util.stream.IntStream.rangeClosed(0,99).mapToObj(n -> "BC%02d".formatted(n)).filter(n -> !usadas.contains(n)).findFirst().orElseThrow());
                }
                if(tipo==TipoSerie.NOTA_CREDITO_BOLETA) serieNota=s.getSerie(); series.saveAndFlush(s);
            }
        });
        config.setHabilitada(true);
    }
    @AfterEach void limpiar() {
        config.setHabilitada(false); config.setEmpresa(emisorAnterior); config.setToken(""); config.setCompanyId(null); SecurityContextHolder.clearContext();
        if(localId==null) return;
        new TransactionTemplate(manager).executeWithoutResult(s -> {
            jdbc.update("delete from comprobante_detalles where comprobante_id in (select id from comprobantes where local_id=?)",localId);
            jdbc.update("delete from comprobantes where local_id=? and referencia_id is not null",localId);
            jdbc.update("delete from comprobantes where local_id=?",localId);
            jdbc.update("delete from series_comprobante where local_id=?",localId);
            jdbc.update("delete from movimientos_stock where local_id=?",localId);
            jdbc.update("delete from devoluciones_venta where venta_id=?",ventaId);
            jdbc.update("delete from pagos_venta where venta_id=?",ventaId);
            jdbc.update("delete from detalles_orden where venta_id=?",ventaId);
            jdbc.update("delete from ventas where local_id=?",localId);
            jdbc.update("delete from stock_bebidas where local_id=?",localId);
            jdbc.update("delete from bebidas where id=?",bebidaId);
            jdbc.update("delete from sesiones_caja where local_id=?",localId);
            jdbc.update("delete from usuarios where empleado_id=?",empleadoId);
            empleados.deleteById(empleadoId); empleados.flush(); locales.deleteById(localId);
        });
    }
    void ambiente(String valor) { server.expect(requestTo("https://facturacion.test/api/v1/companies/42")).andRespond(withSuccess("{\"environment\":\""+valor+"\"}",MediaType.APPLICATION_JSON)); }
    PagosVentaRequestDTO pago() { return new PagosVentaRequestDTO(List.of(new PagoRequestDTO("YAPE",new BigDecimal("118.00"),null)),new ComprobanteRequestDTO(TipoSolicitudComprobante.BOLETA,null,null,null,null)); }
    @ParameterizedTest @ValueSource(strings={"ACEPTADO","RECHAZADO","CAIDA","DISCREPANCIA"})
    void ventaPermanecePagadaTrasCommitConApiSimulada(String resultado) {
        ambiente(resultado.equals("DISCREPANCIA")?"produccion":"beta");
        if(!resultado.equals("DISCREPANCIA")) {
            var req=server.expect(requestTo("https://facturacion.test/api/v1/invoice/send"));
            if(resultado.equals("CAIDA")) req.andRespond(withServerError());
            else req.andRespond(withSuccess("{\"xml\":\"<xml/>\",\"sunatResponse\":{\"cdrResponse\":{\"accepted\":"+resultado.equals("ACEPTADO")+",\"code\":\""+(resultado.equals("ACEPTADO")?"0":"2001")+"\",\"notes\":[]}}}",MediaType.APPLICATION_JSON));
        }
        ventaService.cobrar(ventaId,pago());
        assertEquals(EstadoVenta.PAGADA,ventas.findById(ventaId).orElseThrow().getEstado());
        var c=comprobantes.findByVentaIdAndEstadoNot(ventaId,EstadoComprobante.ANULADO).orElseThrow();
        assertEquals(resultado.equals("DISCREPANCIA")?EstadoComprobante.PENDIENTE:resultado.equals("CAIDA")?EstadoComprobante.ERROR:EstadoComprobante.valueOf(resultado),c.getEstado());
        server.verify();
    }
    @Test void ventaAceptadaAlAnularReservaYEnviaNota() {
        ambiente("beta");
        String ok="{\"sunatResponse\":{\"cdrResponse\":{\"accepted\":true,\"code\":\"0\",\"notes\":[]}}}";
        server.expect(requestTo("https://facturacion.test/api/v1/invoice/send")).andRespond(withSuccess(ok,MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://facturacion.test/api/v1/note/send")).andExpect(jsonPath("$.serie").value(serieNota)).andRespond(withSuccess(ok,MediaType.APPLICATION_JSON));
        ventaService.cobrar(ventaId,pago());
        Long original=comprobantes.findByVentaIdAndEstadoNot(ventaId,EstadoComprobante.ANULADO).orElseThrow().getId();
        ventaService.anular(ventaId,new AnularRequestDTO("Anulación para prueba electrónica"));
        assertEquals(EstadoComprobante.ANULADO,comprobantes.findById(original).orElseThrow().getEstado());
        assertTrue(comprobantes.findAll().stream().anyMatch(c -> original.equals(c.getReferenciaId()) && c.getEstado()==EstadoComprobante.ACEPTADO)); server.verify();
    }
}
