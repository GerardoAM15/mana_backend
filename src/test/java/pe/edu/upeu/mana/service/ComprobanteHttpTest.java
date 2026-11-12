package pe.edu.upeu.mana.service;
import pe.edu.upeu.mana.service.service.EnvioComprobanteService;
import pe.edu.upeu.mana.service.impl.EnvioComprobanteServiceImpl;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.controller.ComprobanteController;
import pe.edu.upeu.mana.entity.Comprobante;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.impl.ComprobanteServiceImpl;
import pe.edu.upeu.mana.service.service.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

class ComprobanteHttpTest {
    FacturacionProperties config=new FacturacionProperties();
    ComprobanteRepository comprobantes=mock(ComprobanteRepository.class);
    ReservaComprobanteService reserva=mock(ReservaComprobanteService.class);
    ContextoLocalService contexto=mock(ContextoLocalService.class);
    VerificadorAmbienteFacturacion ambientes=mock(VerificadorAmbienteFacturacion.class);
    EnvioComprobanteService envio=mock(EnvioComprobanteService.class);
    MockMvc mvc;
    @BeforeEach void preparar() {
        var service=new ComprobanteServiceImpl(comprobantes,mock(ComprobanteDetalleRepository.class),reserva,contexto,new ComprobanteMapper(config),
            mock(ComprobantePayloadMapper.class),mock(ApisPeruFacturacionCliente.class),envio,ambientes,config,mock(CorreoComprobante.class));
        mvc=MockMvcBuilders.standaloneSetup(new ComprobanteController(service)).setControllerAdvice(new GlobalExceptionHandler(Clock.systemUTC())).build();
    }
    @Test void configuracionSinSecretos() throws Exception {
        config.setHabilitada(true); config.setToken("secreto"); config.getEmpresa().setRuc("20471380904");
        mvc.perform(get("/api/v1/comprobantes/configuracion")).andExpect(status().isOk())
            .andExpect(jsonPath("$.habilitada").value(true)).andExpect(jsonPath("$.ambiente").value("BETA"))
            .andExpect(jsonPath("$.boletaMontoIdentificacion").value(700.00)).andExpect(jsonPath("$.rucEmisor").value("20471380904"))
            .andExpect(jsonPath("$.token").doesNotExist());
    }
    @Test void emisionDeshabilitada409() throws Exception {
        mvc.perform(post("/api/v1/comprobantes/ventas/1").contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"BOLETA\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("La facturación electrónica no está habilitada"));
        verifyNoInteractions(reserva);
    }
    @Test void facturaSinRuc400() throws Exception {
        config.setHabilitada(true);
        when(reserva.venta(eq(1L),any())).thenAnswer(a -> { new ValidadorReceptorComprobante(config,mock(ConsultaDocumentoService.class)).validar(a.getArgument(1),new java.math.BigDecimal("10")); return 9L; });
        mvc.perform(post("/api/v1/comprobantes/ventas/1").contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"FACTURA\"}")).andExpect(status().isBadRequest());
    }
    @Test void boletaUmbralSinDocumento400() throws Exception {
        config.setHabilitada(true);
        when(reserva.venta(eq(1L),any())).thenAnswer(a -> { new ValidadorReceptorComprobante(config,mock(ConsultaDocumentoService.class)).validar(a.getArgument(1),new java.math.BigDecimal("700")); return 9L; });
        mvc.perform(post("/api/v1/comprobantes/ventas/1").contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"BOLETA\"}")).andExpect(status().isBadRequest());
    }
    @Test void duplicada409() throws Exception {
        config.setHabilitada(true); when(reserva.venta(eq(1L),any())).thenThrow(new ReglaNegocioException("La venta ya tiene un comprobante vigente"));
        mvc.perform(post("/api/v1/comprobantes/ventas/1").contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"BOLETA\"}")).andExpect(status().isConflict());
    }
    @Test void discrepancia409OcurreDespuesDeReservar() throws Exception {
        config.setHabilitada(true); when(reserva.venta(eq(1L),any())).thenReturn(9L);
        when(ambientes.verificar()).thenReturn(new VerificadorAmbienteFacturacion.Resultado(VerificadorAmbienteFacturacion.Estado.DISCREPANCIA,"Ambiente no coincide"));
        mvc.perform(post("/api/v1/comprobantes/ventas/1").contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"BOLETA\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("Ambiente no coincide"));
        var orden=inOrder(reserva,ambientes); orden.verify(reserva).venta(eq(1L),any()); orden.verify(ambientes).verificar();
    }
    @Test void lecturaDeOtroLocal403() throws Exception {
        var c=new Comprobante(); c.setLocalId(2L); when(comprobantes.findById(1L)).thenReturn(Optional.of(c));
        doThrow(new org.springframework.security.access.AccessDeniedException("Local ajeno")).when(contexto).validarLocal(2L);
        mvc.perform(get("/api/v1/comprobantes/1")).andExpect(status().isForbidden());
    }
    @Test void xmlSinFirmar404() throws Exception {
        when(comprobantes.findById(1L)).thenReturn(Optional.of(new Comprobante())); mvc.perform(get("/api/v1/comprobantes/1/xml")).andExpect(status().isNotFound());
    }
}
