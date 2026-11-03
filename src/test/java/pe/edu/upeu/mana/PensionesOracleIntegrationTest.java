package pe.edu.upeu.mana;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.*;
import static org.junit.jupiter.api.Assertions.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.caja.AbrirCajaRequestDTO;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.caja.CerrarCajaRequestDTO;
import pe.edu.upeu.mana.dto.inventario.CompraBebidaRequestDTO;
import pe.edu.upeu.mana.dto.consumo.ConsumoRequestDTO;
import pe.edu.upeu.mana.dto.caja.ConteoCajaRequestDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoRequestDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoResponseDTO;
import pe.edu.upeu.mana.dto.inventario.DetalleCompraRequestDTO;
import pe.edu.upeu.mana.dto.gasto.GastoDiarioRequestDTO;
import pe.edu.upeu.mana.dto.venta.LineaVentaRequestDTO;
import pe.edu.upeu.mana.dto.menu.MenuRequestDTO;
import pe.edu.upeu.mana.dto.inventario.MovimientoStockResponseDTO;
import pe.edu.upeu.mana.dto.comun.PagoRequestDTO;
import pe.edu.upeu.mana.dto.venta.PagosVentaRequestDTO;
import pe.edu.upeu.mana.dto.consumo.PlatoCantidadRequestDTO;
import pe.edu.upeu.mana.dto.proveedor.ProveedorRequestDTO;
import pe.edu.upeu.mana.dto.contrato.RenovarContratoRequestDTO;
import pe.edu.upeu.mana.dto.contrato.SimularContratoRequestDTO;
import pe.edu.upeu.mana.dto.venta.VentaRequestDTO;
import pe.edu.upeu.mana.enums.EstadoContrato;
import pe.edu.upeu.mana.enums.EstadoVenta;
import pe.edu.upeu.mana.enums.PeriodoPlan;
import pe.edu.upeu.mana.enums.RolUsuario;
import pe.edu.upeu.mana.enums.TipoCorrelativo;
import pe.edu.upeu.mana.enums.TipoLineaVenta;
import pe.edu.upeu.mana.enums.TipoMovimientoStock;
import pe.edu.upeu.mana.enums.TipoPagoContrato;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@EnabledIfEnvironmentVariable(named = "MANA_ORACLE_TEST", matches = "true")
@Transactional
class PensionesOracleIntegrationTest {
    @Autowired pe.edu.upeu.mana.service.service.ClienteService clientesService;

    @Test void listadosGrandesPaginanEnLaBaseDeDatos() {
        var contrato=registrar();
        assertEquals(1L,contratos.listar(null,EstadoContrato.ACTIVO,null,null,null,0,1,"fechaInicio","asc").getTotalElementos());
        assertEquals(contrato.getId(),contratos.listar(null,EstadoContrato.ACTIVO,null,null,null,0,1,"id","asc").getContenido().getFirst().getId());
        assertTrue(clientesService.listar(0,1,"dni","asc").getContenido().size()<=1);
        assertEquals(1L,cajas.listar(null,null,null,null,0,1,"apertura","desc").getTotalElementos());
        assertEquals(0L,ventasService.listar(null,null,null,null,null,0,1,"total","desc").getTotalElementos());
        assertEquals(0L,consumos.listar(null,null,null,null,null,0,1,"fecha","asc").getTotalElementos());
        gastos.crear(new GastoDiarioRequestDTO(LocalDate.now(clock),new BigDecimal("1.00"),"Prueba de paginación",true));
        assertEquals(1L,gastos.listar(null,null,null,0,1,"monto","desc").getTotalElementos());
        assertEquals(0L,inventario.listarMovimientos(null,null,null,null,0,1,"fecha","asc").getTotalElementos());
        assertThrows(ReglaNegocioException.class,()->contratos.listar(null,null,null,null,null,0,20,"claveHash","asc"));
    }

    @Autowired
    ContratoService contratos;

    @Autowired
    ConsumoService consumos;

    @Autowired
    CajaService cajas;

    @Autowired
    MenuService menus;

    @Autowired
    LocalRepository locales;

    @Autowired
    CorrelativoRepository correlativos;

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
    PlatoRepository platos;

    @Autowired
    ContratoRepository contratosRepository;

    @Autowired
    DetalleContratoRepository detalles;

    @Autowired
    ServicioDiaAtencionRepository dias;

    @Autowired
    TarifaServicioRepository tarifas;

    @Autowired
    Clock clock;

    @Autowired
    VentaService ventasService;

    @Autowired
    BebidaRepository bebidas;

    @Autowired
    StockBebidaRepository stockBebidas;

    @Autowired
    ReglaCobroRepository reglas;

    @Autowired
    ProveedorService proveedoresService;

    @Autowired
    InventarioService inventario;

    @Autowired
    GastoDiarioService gastos;

    @Autowired
    ReporteService reportes;

    Long localId, clienteId, planId;

    @BeforeEach
    void preparar() {
        var local = new Local();
        local.setNombre("Prueba pensiones " + UUID.randomUUID());
        locales.saveAndFlush(local);
        localId = local.getId();
        var correlativo = new Correlativo();
        correlativo.setLocalId(localId);
        correlativo.setTipo(TipoCorrelativo.CONTRATO);
        correlativo.setUltimoNumero(0L);
        correlativos.saveAndFlush(correlativo);
        var cv = new Correlativo();
        cv.setLocalId(localId);
        cv.setTipo(TipoCorrelativo.VENTA);
        cv.setUltimoNumero(0L);
        correlativos.saveAndFlush(cv);
        var empleado = new Empleado();
        empleado.setDni(dni());
        empleado.setNombre("Prueba");
        empleado.setApellido("Pensiones");
        empleado.setCargo("Administrador");
        empleado.setLocalId(localId);
        empleados.saveAndFlush(empleado);
        var usuario = new Usuario();
        usuario.setEmpleadoId(empleado.getId());
        usuario.setCorreo("test-" + UUID.randomUUID() + "@example.invalid");
        usuario.setClaveHash("hash-no-utilizado-en-login");
        usuario.setRol(RolUsuario.ADMINISTRADOR);
        usuario.setTokenVersion(0L);
        usuarios.saveAndFlush(usuario);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario.getCorreo(), null, List.of()));
        var cliente = new Cliente();
        cliente.setDni(dni());
        cliente.setNombre("Pensionista");
        cliente.setApellido("Prueba");
        clientes.saveAndFlush(cliente);
        clienteId = cliente.getId();
        planId = planes.findAll().stream().filter(p -> p.getPeriodo() == PeriodoPlan.SEMANAL && Boolean.TRUE.equals(p.getActivo())).findFirst().orElseThrow().getId();
        cajas.abrir(new AbrirCajaRequestDTO(new BigDecimal("0.00")));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registraPrepagoConsumeYAnulaSinPerderLaAuditoria() {
        Assumptions.assumeTrue(dias.existsById(new ServicioDiaAtencionId("D", LocalDate.now(clock).getDayOfWeek().getValue())));
        var c = registrar();
        assertEquals(EstadoContrato.ACTIVO, c.getEstado());
        assertEquals("C-000001", c.getNumero());
        assertEquals(c.getMontoTotal(), c.getPagos().getFirst().getMonto());
        assertEquals(c.getDetalles().getFirst().getRacionesContratadas(), c.getDetalles().getFirst().getSaldoRaciones());
        assertEquals(c.getId(), contratos.buscarVigente(clienteId, null).getId());
        String dni = clientes.findById(clienteId).orElseThrow().getDni();
        assertEquals(c.getId(), contratos.buscarPensionistas(dni, null).getFirst().getContratoId());
        var pan = plato("PAN");
        var bebida = plato("BEBIDA_DESAYUNO");
        menus.guardar(new MenuRequestDTO(LocalDate.now(clock), "D", Set.of(pan.getId(), bebida.getId())));
        // Cambiar la tarifa no debe alterar el precio pactado ni el valor del consumo.
        var tarifa = tarifas.findByServicioAndActivoTrue("D").orElseThrow();
        tarifa.setPrecio(tarifa.getPrecio().add(BigDecimal.ONE));
        tarifas.flush();
        var consumo = consumos.crear(new ConsumoRequestDTO(c.getId(), "D", List.of(new PlatoCantidadRequestDTO(pan.getId(), 2L), new PlatoCantidadRequestDTO(bebida.getId(), 1L)), true, null));
        assertEquals(c.getDetalles().getFirst().getPrecioRacionPactado(), consumo.getValorRacion());
        assertEquals(0, consumo.getRecargoLlevar().signum());
        assertNull(consumo.getCajaId());
        assertEquals(c.getDetalles().getFirst().getSaldoRaciones() - 1, contratos.buscar(c.getId()).getDetalles().getFirst().getSaldoRaciones());
        var anulado = consumos.anular(consumo.getId(), new AnularRequestDTO("Corrección de consumo"));
        assertTrue(anulado.getAnulado());
        assertEquals(c.getDetalles().getFirst().getSaldoRaciones(), contratos.buscar(c.getId()).getDetalles().getFirst().getSaldoRaciones());
        assertThrows(ReglaNegocioException.class, () -> contratos.anular(c.getId(), new AnularRequestDTO("Anulación del contrato")));
    }

    @Test
    void devuelveYapeEnOtraCajaYCierraConEsperadoNegativo() {
        var c = registrar();
        var origen = cajas.buscarActual();
        cajas.cerrar(origen.getId(), new CerrarCajaRequestDTO(List.of(new ConteoCajaRequestDTO("YAPE", c.getMontoTotal())), null));
        var destino = cajas.abrir(new AbrirCajaRequestDTO(new BigDecimal("0.00")));
        var anulado = contratos.anular(c.getId(), new AnularRequestDTO("Devolución de contrato"));
        assertEquals(EstadoContrato.ANULADO, anulado.getEstado());
        var devoluciones = anulado.getPagos().stream().filter(p -> p.getTipo() == TipoPagoContrato.DEVOLUCION).toList();
        assertEquals(1, devoluciones.size());
        assertEquals(destino.getId(), devoluciones.getFirst().getCajaId());
        var cierre = cajas.cerrar(destino.getId(), new CerrarCajaRequestDTO(List.of(new ConteoCajaRequestDTO("YAPE", BigDecimal.ZERO)), null));
        assertEquals(c.getMontoTotal().negate(), cierre.getMontoEsperado());
        assertEquals(c.getMontoTotal(), cierre.getDiferencia());
    }

    @Test
    void renuevaDesdeMananaSinTrasladarRaciones() {
        var hoy = LocalDate.now(clock);
        var previo = new Contrato();
        previo.setNumero("C-ANTERIOR");
        previo.setClienteId(clienteId);
        previo.setLocalId(localId);
        previo.setPlanId(planId);
        previo.setEstado(EstadoContrato.ACTIVO);
        previo.setFechaInicio(hoy.minusDays(6));
        previo.setFechaFin(hoy);
        previo.setMontoTotal(new BigDecimal("50.00"));
        contratosRepository.saveAndFlush(previo);
        var d = new DetalleContrato();
        d.setContratoId(previo.getId());
        d.setServicio("D");
        d.setRacionesContratadas(5L);
        d.setSaldoRaciones(1L);
        d.setPrecioRacionPactado(new BigDecimal("10.00"));
        d.setSubtotal(new BigDecimal("50.00"));
        detalles.saveAndFlush(d);
        var simulacion = contratos.simular(new SimularContratoRequestDTO(clienteId, planId, hoy.plusDays(1), List.of("D")));
        var nuevo = contratos.renovar(previo.getId(), new RenovarContratoRequestDTO(planId, List.of("D"), List.of(new PagoRequestDTO("YAPE", simulacion.getTotal(), null))));
        assertEquals(previo.getId(), nuevo.getAnteriorId());
        assertEquals(hoy.plusDays(1), nuevo.getFechaInicio());
        assertEquals(EstadoContrato.PROGRAMADO, nuevo.getEstado());
        assertEquals(1L, detalles.findById(d.getId()).orElseThrow().getSaldoRaciones());
        assertEquals(nuevo.getDetalles().getFirst().getRacionesContratadas(), nuevo.getDetalles().getFirst().getSaldoRaciones());
    }

    private ContratoResponseDTO registrar() {
        var hoy = LocalDate.now(clock);
        var simulacion = contratos.simular(new SimularContratoRequestDTO(clienteId, planId, hoy, List.of("D")));
        return contratos.crear(new ContratoRequestDTO(clienteId, planId, hoy, List.of("D"), List.of(new PagoRequestDTO("YAPE", simulacion.getTotal(), "prueba"))));
    }

    @Test
    void reportesSumanIngresosNetosYGastosEnLaBaseDeDatos() {
        var contrato = registrar();
        var hoy = LocalDate.now(clock);
        var b = new Bebida();
        b.setNombre("Prueba reportes");
        b.setPrecioVenta(new BigDecimal("2.00"));
        bebidas.saveAndFlush(b);
        var s = new StockBebida();
        s.setLocalId(localId);
        s.setBebidaId(b.getId());
        s.setStock(5L);
        stockBebidas.saveAndFlush(s);
        var venta = ventasService.crear(new VentaRequestDTO("A", false, null, List.of(new LineaVentaRequestDTO(TipoLineaVenta.BEBIDA, 2L, null, null, b.getId(), null)), List.of(new PagoRequestDTO("YAPE", new BigDecimal("4.00"), null))));
        gastos.crear(new GastoDiarioRequestDTO(hoy, new BigDecimal("3.00"), "Gasto para reporte", true));
        var ingresos = reportes.ingresos(hoy, hoy, null).getFirst();
        assertEquals(contrato.getMontoTotal(), ingresos.getContratos());
        assertEquals(new BigDecimal("4.00"), ingresos.getVentas());
        assertEquals(contrato.getMontoTotal().add(new BigDecimal("1.00")), reportes.gastos(hoy, hoy, null, "DIA").getFirst().getDiferencia());
        assertEquals(new BigDecimal("3.00"), reportes.gastos(hoy, hoy, null, "MES").getFirst().getGastos());
        assertFalse(reportes.porVencer(7, null, null, null).isEmpty());
        assertTrue(reportes.platos(hoy, hoy, null, null).isEmpty());
        assertTrue(reportes.asistencia(hoy, hoy, null).isEmpty());
        ventasService.anular(venta.getId(), new AnularRequestDTO("Anulación para reporte"));
        assertEquals(new BigDecimal("0.00"), reportes.ingresos(hoy, hoy, null).getFirst().getVentas());
        contratos.anular(contrato.getId(), new AnularRequestDTO("Anulación para reporte"));
        assertEquals(new BigDecimal("0.00"), reportes.ingresos(hoy, hoy, null).getFirst().getTotal());
    }

    @Test
    void compraVentaYDevolucionMantienenElKardexYElStock() {
        var b = new Bebida();
        b.setNombre("Prueba inventario");
        b.setPrecioVenta(new BigDecimal("2.00"));
        bebidas.saveAndFlush(b);
        var proveedor = proveedoresService.crear(new ProveedorRequestDTO("Proveedor de prueba", null, true, Set.of(b.getId())));
        var compra = inventario.crear(new CompraBebidaRequestDTO(proveedor.getId(), LocalDate.now(clock), List.of(new DetalleCompraRequestDTO(b.getId(), 2L, 6L, new BigDecimal("18.00")))));
        assertEquals(new BigDecimal("18.00"), compra.getTotal());
        assertEquals(12L, compra.getDetalles().getFirst().getUnidades());
        assertEquals(12L, inventario.listarStock(null).getFirst().getStock());
        var venta = ventasService.crear(new VentaRequestDTO("A", true, null, List.of(new LineaVentaRequestDTO(TipoLineaVenta.BEBIDA, 2L, null, null, b.getId(), null)), List.of(new PagoRequestDTO("YAPE", new BigDecimal("4.00"), null))));
        assertEquals(10L, inventario.listarStock(null).getFirst().getStock());
        ventasService.anular(venta.getId(), new AnularRequestDTO("Devolución de prueba"));
        assertEquals(12L, inventario.listarStock(null).getFirst().getStock());
        assertEquals(List.of(TipoMovimientoStock.COMPRA, TipoMovimientoStock.VENTA, TipoMovimientoStock.DEVOLUCION), inventario.listarMovimientos(null, b.getId(), LocalDate.now(clock), LocalDate.now(clock),0,20,"fecha","asc").getContenido().stream().map(MovimientoStockResponseDTO::getTipo).toList());
    }

    @Test
    void gastoSeEditaYSeDesactivaDentroDeSuLocal() {
        var g = gastos.crear(new GastoDiarioRequestDTO(LocalDate.now(clock), new BigDecimal("15.00"), "Gasto de prueba", true));
        assertEquals(localId, g.getLocalId());
        var editado = gastos.actualizar(g.getId(), new GastoDiarioRequestDTO(LocalDate.now(clock), new BigDecimal("12.00"), "Gasto corregido", true));
        assertEquals(new BigDecimal("12.00"), editado.getMonto());
        gastos.eliminar(g.getId());
        assertFalse(gastos.buscar(g.getId()).getActivo());
    }

    @Test
    void ventaBebidaPendienteCobroYAnulacionReponenStock() {
        var b = new Bebida();
        b.setNombre("Prueba bebida");
        b.setPrecioVenta(new BigDecimal("2.00"));
        bebidas.saveAndFlush(b);
        var s = new StockBebida();
        s.setBebidaId(b.getId());
        s.setLocalId(localId);
        s.setStock(10L);
        stockBebidas.saveAndFlush(s);
        var venta = ventasService.crear(new VentaRequestDTO("A", true, null, List.of(new LineaVentaRequestDTO(TipoLineaVenta.BEBIDA, 2L, null, null, b.getId(), null)), null));
        assertEquals("V-000001", venta.getNumero());
        assertEquals(EstadoVenta.PENDIENTE, venta.getEstado());
        assertEquals(new BigDecimal("4.00"), venta.getTotal());
        assertEquals(0, venta.getRecargoLlevar().signum());
        assertEquals(8L, stockBebidas.findById(s.getId()).orElseThrow().getStock());
        var pagada = ventasService.cobrar(venta.getId(), new PagosVentaRequestDTO(List.of(new PagoRequestDTO("YAPE", venta.getTotal(), null))));
        assertEquals(EstadoVenta.PAGADA, pagada.getEstado());
        var anulada = ventasService.anular(venta.getId(), new AnularRequestDTO("Devolución de venta"));
        assertEquals(EstadoVenta.ANULADA, anulada.getEstado());
        assertEquals(1, anulada.getDevoluciones().size());
        assertEquals(10L, stockBebidas.findById(s.getId()).orElseThrow().getStock());
        assertEquals(0, cajas.resumir(cajas.buscarActual().getId()).getMontoEsperado().signum());
    }

    @Test
    void comboCantidadDosGuardaLaComposicionDeUnaUnidad() {
        var pan = plato("PAN");
        var bebida = plato("BEBIDA_DESAYUNO");
        menus.guardar(new MenuRequestDTO(LocalDate.now(clock), "D", Set.of(pan.getId(), bebida.getId())));
        var regla = reglas.findByServicioAndActivoTrueOrderById("D").getFirst();
        var total = regla.getPrecioCombo().multiply(BigDecimal.valueOf(2)).setScale(2);
        var venta = ventasService.crear(new VentaRequestDTO("D", true, null, List.of(new LineaVentaRequestDTO(TipoLineaVenta.COMBO, 2L, null, regla.getId(), null, List.of(new PlatoCantidadRequestDTO(pan.getId(), 2L), new PlatoCantidadRequestDTO(bebida.getId(), 1L)))), List.of(new PagoRequestDTO("YAPE", total, null))));
        assertEquals(EstadoVenta.PAGADA, venta.getEstado());
        assertEquals(total, venta.getTotal());
        assertEquals(2L, venta.getLineas().getFirst().getPlatos().stream().filter(p -> p.getPlatoId().equals(pan.getId())).findFirst().orElseThrow().getCantidad());
        assertFalse(ventasService.catalogo("D").getSueltos().isEmpty());
    }

    private Plato plato(String categoria) {
        var p = new Plato();
        p.setNombre("Prueba " + categoria);
        p.setCategoria(categoria);
        return platos.saveAndFlush(p);
    }

    private String dni() {
        String valor;
        do {
            valor = "%08d".formatted(java.util.concurrent.ThreadLocalRandom.current().nextInt(100_000_000));
        } while (personas.existsByDniAndIdNot(valor, -1L));
        return valor;
    }
}
