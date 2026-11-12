package pe.edu.upeu.mana;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.*;
import static org.junit.jupiter.api.Assertions.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.caja.CerrarCajaRequestDTO;
import pe.edu.upeu.mana.dto.caja.ConteoCajaRequestDTO;
import pe.edu.upeu.mana.enums.EstadoContrato;
import pe.edu.upeu.mana.enums.EstadoVenta;
import pe.edu.upeu.mana.enums.TipoPagoContrato;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@EnabledIfEnvironmentVariable(named = "MANA_ORACLE_TEST", matches = "true")
class CajaOracleIntegrationTest {

    @Autowired
    SesionCajaRepository cajas;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    EmpleadoRepository empleados;

    @Autowired
    ClienteRepository clientes;

    @Autowired
    PersonaRepository personas;

    @Autowired
    TipoPlanRepository planes;

    @Autowired
    ContratoRepository contratos;

    @Autowired
    DetalleContratoRepository detallesContrato;

    @Autowired
    PagoContratoRepository pagosContrato;

    @Autowired
    VentaRepository ventas;

    @Autowired
    PagoVentaRepository pagosVenta;

    @Autowired
    DevolucionVentaRepository devoluciones;

    @Autowired
    ConsumoRepository consumos;

    @Autowired
    DetalleCierreCajaRepository detallesCierre;

    @Autowired
    CajaService service;

    @Autowired
    Clock clock;

    @Value("${app.admin.email}")
    String correo;

    @Test
    @Transactional
    void sumaTodosLosMovimientosYCierraConEsperadoNegativoEnOracle() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(correo, null, List.of()));
        try {
            var u = usuarios.findByCorreoIgnoreCase(correo).orElseThrow();
            Long local = empleados.findById(u.getEmpleadoId()).orElseThrow().getLocalId();
            var caja = sesion(u.getId(), local, false);
            var anterior = sesion(u.getId(), local, true);
            String dni;
            do {
                dni = "%08d".formatted(java.util.concurrent.ThreadLocalRandom.current().nextInt(100_000_000));
            } while (personas.existsByDniAndIdNot(dni, -1L));
            var cliente = new Cliente();
            cliente.setDni(dni);
            cliente.setNombre("Prueba");
            cliente.setApellido("Caja");
            clientes.saveAndFlush(cliente);
            var contrato = new Contrato();
            contrato.setNumero(numero());
            contrato.setClienteId(cliente.getId());
            contrato.setLocalId(local);
            contrato.setPlanId(planes.findAll().getFirst().getId());
            contrato.setEstado(EstadoContrato.ACTIVO);
            contrato.setFechaInicio(LocalDate.now(clock));
            contrato.setFechaFin(LocalDate.now(clock).plusDays(6));
            contrato.setMontoTotal(monto("100"));
            contratos.saveAndFlush(contrato);
            var detalle = new DetalleContrato();
            detalle.setContratoId(contrato.getId());
            detalle.setServicio("A");
            detalle.setRacionesContratadas(10L);
            detalle.setSaldoRaciones(9L);
            detalle.setPrecioRacionPactado(monto("10"));
            detalle.setSubtotal(monto("100"));
            detallesContrato.saveAndFlush(detalle);
            pagoContrato(contrato.getId(), anterior.getId(), TipoPagoContrato.COBRO);
            pagoContrato(contrato.getId(), caja.getId(), TipoPagoContrato.DEVOLUCION);
            var venta = new Venta();
            venta.setNumero(numero());
            venta.setLocalId(local);
            venta.setCajaId(caja.getId());
            venta.setServicio("A");
            venta.setParaLlevar(false);
            venta.setEstado(EstadoVenta.ANULADA);
            venta.setFecha(LocalDateTime.now(clock));
            venta.setRecargoLlevar(monto("0"));
            venta.setTotal(monto("40"));
            venta.setAnuladaEn(LocalDateTime.now(clock));
            venta.setMotivoAnulacion("Prueba de devolución");
            ventas.saveAndFlush(venta);
            var pago = new PagoVenta();
            pago.setVentaId(venta.getId());
            pago.setMonto(monto("40"));
            pago.setMetodo("EFECTIVO");
            pago.setFecha(LocalDateTime.now(clock));
            pagosVenta.saveAndFlush(pago);
            var devolucion = new DevolucionVenta();
            devolucion.setVentaId(venta.getId());
            devolucion.setMonto(monto("40"));
            devolucion.setMetodo("EFECTIVO");
            devolucion.setFecha(LocalDateTime.now(clock));
            devolucion.setMotivo("Prueba de devolución");
            devoluciones.saveAndFlush(devolucion);
            // Otra venta pagada mantiene un ingreso neto en efectivo de 60.
            var pagada = new Venta();
            pagada.setNumero(numero());
            pagada.setLocalId(local);
            pagada.setCajaId(caja.getId());
            pagada.setServicio("A");
            pagada.setParaLlevar(false);
            pagada.setEstado(EstadoVenta.PAGADA);
            pagada.setFecha(LocalDateTime.now(clock));
            pagada.setRecargoLlevar(monto("0"));
            pagada.setTotal(monto("60"));
            ventas.saveAndFlush(pagada);
            var cobro = new PagoVenta();
            cobro.setVentaId(pagada.getId());
            cobro.setMonto(monto("60"));
            cobro.setMetodo("EFECTIVO");
            cobro.setFecha(LocalDateTime.now(clock));
            pagosVenta.saveAndFlush(cobro);
            consumo(detalle.getId(), caja.getId(), false, "2");
            consumo(detalle.getId(), caja.getId(), true, "70");
            var resumen = service.resumir(caja.getId());
            assertEquals(monto("-28"), resumen.getMontoEsperado());
            assertEquals(2L, resumen.getVentas());
            assertEquals(1L, resumen.getConsumos());
            assertEquals(1L, resumen.getContratos());
            assertEquals(monto("72"), resumen.getMetodos().stream().filter(m -> m.getMetodo().equals("EFECTIVO")).findFirst().orElseThrow().getMontoEsperado());
            assertEquals(monto("-100"), resumen.getMetodos().stream().filter(m -> m.getMetodo().equals("YAPE")).findFirst().orElseThrow().getMontoEsperado());
            service.validarDevolucion(caja.getId(), Map.of("YAPE", monto("1000")));
            var cierre = service.cerrar(caja.getId(), new CerrarCajaRequestDTO(List.of(new ConteoCajaRequestDTO("EFECTIVO", monto("72")), new ConteoCajaRequestDTO("YAPE", monto("0"))), null));
            assertEquals(monto("-28"), cierre.getMontoEsperado());
            assertEquals(monto("72"), cierre.getMontoContado());
            assertEquals(monto("100"), cierre.getDiferencia());
            assertEquals(monto("-100"), detallesCierre.findBySesionCajaIdOrderByMetodo(caja.getId()).stream().filter(d -> d.getMetodo().equals("YAPE")).findFirst().orElseThrow().getMontoEsperado());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @Transactional
    void restriccionesOracleAceptanCierreNegativoYContadoCero() {
        var u = usuarios.findByCorreoIgnoreCase(correo).orElseThrow();
        var caja = sesion(u.getId(), empleados.findById(u.getEmpleadoId()).orElseThrow().getLocalId(), false);
        caja.setCierre(LocalDateTime.now(clock));
        caja.setClaveAbierta(null);
        caja.setMontoEsperado(monto("-100"));
        caja.setMontoContado(monto("0"));
        caja.setDiferencia(monto("100"));
        cajas.saveAndFlush(caja);
        var d = new DetalleCierreCaja();
        d.setSesionCajaId(caja.getId());
        d.setMetodo("YAPE");
        d.setMontoEsperado(monto("-100"));
        d.setMontoContado(monto("0"));
        d.setDiferencia(monto("100"));
        assertNotNull(detallesCierre.saveAndFlush(d).getId());
    }

    private SesionCaja sesion(Long usuario, Long local, boolean cerrada) {
        var c = new SesionCaja();
        c.setLocalId(local);
        c.setUsuarioId(usuario);
        c.setApertura(LocalDateTime.now(clock).minusHours(1));
        c.setMontoInicial(cerrada ? monto("0") : monto("10"));
        c.setClaveAbierta("test-" + UUID.randomUUID());
        if (cerrada) {
            c.setCierre(LocalDateTime.now(clock));
            c.setClaveAbierta(null);
            c.setMontoEsperado(monto("100"));
            c.setMontoContado(monto("100"));
            c.setDiferencia(monto("0"));
        }
        return cajas.saveAndFlush(c);
    }

    private void pagoContrato(Long contrato, Long caja, TipoPagoContrato tipo) {
        var p = new PagoContrato();
        p.setContratoId(contrato);
        p.setCajaId(caja);
        p.setTipo(tipo);
        p.setMonto(monto("100"));
        p.setMetodo("YAPE");
        p.setFecha(LocalDateTime.now(clock));
        pagosContrato.saveAndFlush(p);
    }

    private void consumo(Long detalle, Long caja, boolean anulado, String recargo) {
        var c = new Consumo();
        c.setDetalleContratoId(detalle);
        c.setCajaId(caja);
        c.setFecha(LocalDate.now(clock));
        c.setRegistradoEn(LocalDateTime.now(clock));
        c.setValorRacion(monto("10"));
        c.setParaLlevar(true);
        c.setRecargoLlevar(monto(recargo));
        c.setMetodoRecargo("EFECTIVO");
        c.setAnulado(anulado);
        if (anulado) {
            c.setAnuladoEn(LocalDateTime.now(clock));
            c.setMotivoAnulacion("Prueba de anulación");
        } else
            c.setClaveDiaria("test-" + UUID.randomUUID());
        consumos.saveAndFlush(c);
    }

    private String numero() {
        return "T-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private BigDecimal monto(String valor) {
        return new BigDecimal(valor).setScale(2);
    }
}
