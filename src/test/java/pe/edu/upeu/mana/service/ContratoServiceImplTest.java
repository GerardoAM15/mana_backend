package pe.edu.upeu.mana.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.mapper.ContratoMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.impl.ContratoServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoRequestDTO;
import pe.edu.upeu.mana.dto.comun.PagoRequestDTO;
import pe.edu.upeu.mana.dto.contrato.RenovarContratoRequestDTO;
import pe.edu.upeu.mana.enums.EstadoContrato;
import pe.edu.upeu.mana.enums.PeriodoPlan;
import pe.edu.upeu.mana.enums.TipoCorrelativo;
import pe.edu.upeu.mana.enums.TipoPagoContrato;
import pe.edu.upeu.mana.service.service.CajaService;
import pe.edu.upeu.mana.service.service.ConsumoService;
import pe.edu.upeu.mana.service.service.ContextoLocalService;
import pe.edu.upeu.mana.service.service.CorrelativoService;

@ExtendWith(MockitoExtension.class)
class ContratoServiceImplTest {

    @Mock
    ContratoRepository contratos;

    @Mock
    ClienteRepository clientes;

    @Mock
    TipoPlanRepository planes;

    @Mock
    PlanServicioRepository planServicios;

    @Mock
    ServicioRepository servicios;

    @Mock
    ServicioDiaAtencionRepository dias;

    @Mock
    DiaNoAtencionRepository cierres;

    @Mock
    TarifaServicioRepository tarifas;

    @Mock
    DetalleContratoRepository detalles;

    @Mock
    PagoContratoRepository pagos;

    @Mock
    ConsumoRepository consumos;

    @Mock
    ContextoLocalService contexto;

    @Mock
    CajaService cajas;

    @Mock
    CorrelativoService correlativos;

    @Mock
    ValidadorPagos validador;

    @Mock
    ConsumoService consultaConsumos;

    @Mock
    ContratoMapper mapper;

    Clock clock = Clock.fixed(Instant.parse("2026-11-02T15:00:00Z"), ZoneId.of("America/Lima"));

    ContratoServiceImpl service;

    Cliente cliente;

    Contrato anterior;

    SesionCaja caja;

    @BeforeEach
    void preparar() {
        service = crearServicio(clock);
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678");
        caja = new SesionCaja();
        caja.setId(5L);
        caja.setLocalId(1L);
        anterior = new Contrato();
        anterior.setId(10L);
        anterior.setNumero("C-000001");
        anterior.setClienteId(1L);
        anterior.setLocalId(1L);
        anterior.setFechaInicio(LocalDate.parse("2026-11-02"));
        anterior.setFechaFin(LocalDate.parse("2026-11-08"));
        anterior.setEstado(EstadoContrato.ACTIVO);
    }

    private ContratoServiceImpl crearServicio(Clock reloj) {
        return new ContratoServiceImpl(contratos, clientes, planes, planServicios, servicios, dias, cierres, tarifas, detalles, pagos, consumos, contexto, cajas, new CalculadoraRaciones(), correlativos, validador, new EstadoContratoActual(reloj), consultaConsumos, mapper, reloj);
    }

    private void crearBase() {
        when(cajas.buscarActualParaActualizar()).thenReturn(caja);
        when(clientes.buscarParaActualizar(1L)).thenReturn(Optional.of(cliente));
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        var plan = new TipoPlan();
        plan.setId(2L);
        plan.setPeriodo(PeriodoPlan.SEMANAL);
        when(planes.findById(2L)).thenReturn(Optional.of(plan));
    }

    private void contratoBase() {
        when(cajas.buscarActualParaActualizar()).thenReturn(caja);
        when(contratos.buscarClienteId(10L)).thenReturn(Optional.of(1L));
        when(clientes.buscarParaActualizar(1L)).thenReturn(Optional.of(cliente));
        when(contratos.buscarParaActualizar(10L)).thenReturn(Optional.of(anterior));
    }

    @Test
    void superposicionSeRechazaAunqueElOtroContratoSeaDeOtroLocal() {
        crearBase();
        when(contratos.existeSuperposicion(1L, LocalDate.parse("2026-11-02"), LocalDate.parse("2026-11-08"))).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> service.crear(request()));
        verifyNoInteractions(detalles, pagos, correlativos);
    }

    @Test
    void cajaCerradaImpideRegistrarAntesDeGuardarDatos() {
        when(cajas.buscarActualParaActualizar()).thenThrow(new ReglaNegocioException("Debe tener una caja abierta"));
        assertThrows(ReglaNegocioException.class, () -> service.crear(request()));
        verifyNoInteractions(contratos, clientes, detalles, pagos);
    }

    @Test
    void renovacionFueraDeLosUltimosTresDiasEsRechazada() {
        contratoBase();
        assertThrows(ReglaNegocioException.class, () -> service.renovar(10L, new RenovarContratoRequestDTO(2L, List.of("D"), pagos())));
        verify(contratos, never()).saveAndFlush(any());
    }

    @Test
    void renovacionTrasAnularLaAnteriorCreaUnContratoSinTrasladarSaldo() {
        // Una renovación ANULADO no satisface la búsqueda de renovaciones vigentes.
        when(contratos.existsByAnteriorIdAndEstadoNot(10L, EstadoContrato.ANULADO)).thenReturn(false);
        service = crearServicio(Clock.fixed(Instant.parse("2026-11-05T15:00:00Z"), ZoneId.of("America/Lima")));
        contratoBase();
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        var plan = new TipoPlan();
        plan.setId(2L);
        plan.setNombre("Semanal");
        plan.setPeriodo(PeriodoPlan.SEMANAL);
        when(planes.findById(2L)).thenReturn(Optional.of(plan));
        var ps = new PlanServicio();
        ps.setPlanId(2L);
        ps.setServicio("D");
        when(planServicios.findByPlanId(2L)).thenReturn(List.of(ps));
        var s = new Servicio();
        s.setCodigo("D");
        when(servicios.findById("D")).thenReturn(Optional.of(s));
        when(dias.findByServicio("D")).thenReturn(java.util.stream.IntStream.rangeClosed(1, 5).mapToObj(d -> {
            var dia = new ServicioDiaAtencion();
            dia.setServicio("D");
            dia.setDiaSemana(d);
            return dia;
        }).toList());
        var t = new TarifaServicio();
        t.setPrecio(new BigDecimal("3.20"));
        when(tarifas.findByServicioAndActivoTrue("D")).thenReturn(Optional.of(t));
        when(correlativos.siguiente(1L, TipoCorrelativo.CONTRATO)).thenReturn("C-000002");
        var siguienteId = new java.util.concurrent.atomic.AtomicLong(11L);
        when(contratos.saveAndFlush(any())).thenAnswer(i -> {
            Contrato c = i.getArgument(0);
            c.setId(siguienteId.getAndIncrement());
            return c;
        });
        var u = new Usuario();
        u.setCorreo("prueba@example.invalid");
        when(contexto.usuario()).thenReturn(u);
        service.renovar(10L, new RenovarContratoRequestDTO(2L, List.of("D"), pagos()));
        var captor = ArgumentCaptor.forClass(Contrato.class);
        verify(contratos).saveAndFlush(captor.capture());
        assertEquals(LocalDate.parse("2026-11-09"), captor.getValue().getFechaInicio());
        assertEquals(10L, captor.getValue().getAnteriorId());
        assertEquals(EstadoContrato.PROGRAMADO, captor.getValue().getEstado());
        assertEquals(EstadoContrato.ACTIVO, anterior.getEstado());
        verify(detalles).save(argThat(d -> d.getSaldoRaciones() == 5 && d.getRacionesContratadas() == 5 && d.getPrecioRacionPactado().equals(new BigDecimal("3.20"))));
        verify(validador).validar(argThat(p -> p.size()==1 && p.getFirst().getMetodo().equals("YAPE") && p.getFirst().getMonto().compareTo(new BigDecimal("16.00"))==0), eq(new BigDecimal("16.00")));
        var renovado = captor.getValue();
        when(contratos.buscarClienteId(11L)).thenReturn(Optional.of(1L));
        when(contratos.buscarParaActualizar(11L)).thenReturn(Optional.of(renovado));
        service.anular(11L,new AnularRequestDTO("Renovación equivocada"));
        assertEquals(EstadoContrato.ANULADO,renovado.getEstado());
        when(contratos.existsByAnteriorIdAndEstadoNot(10L,EstadoContrato.ANULADO)).thenAnswer(i -> renovado.getEstado()!=EstadoContrato.ANULADO);
        service.renovar(10L,new RenovarContratoRequestDTO(2L,List.of("D"),pagos()));
        verify(contratos,times(2)).saveAndFlush(captor.capture());
        assertEquals(12L,captor.getValue().getId()); assertEquals(10L,captor.getValue().getAnteriorId());
    }

    @Test
    void consumoInclusoAnuladoImpideAnularContrato() {
        contratoBase();
        when(consumos.existePorContrato(10L)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> service.anular(10L, new AnularRequestDTO("Motivo de anulación")));
        verify(pagos, never()).save(any());
    }

    @Test
    void devolucionSinEfectivoNoCambiaElContratoNiGuardaPagos() {
        contratoBase();
        var p = new PagoContrato();
        p.setTipo(TipoPagoContrato.COBRO);
        p.setMetodo("EFECTIVO");
        p.setMonto(new BigDecimal("100.00"));
        when(pagos.findByContratoIdOrderById(10L)).thenReturn(List.of(p));
        doThrow(new ReglaNegocioException("No hay efectivo suficiente en caja para la devolución")).when(cajas).validarDevolucion(5L, Map.of("EFECTIVO", new BigDecimal("100.00")));
        assertThrows(ReglaNegocioException.class, () -> service.anular(10L, new AnularRequestDTO("Motivo de anulación")));
        assertEquals(EstadoContrato.ACTIVO, anterior.getEstado());
        verify(pagos, never()).save(any());
    }

    @Test void renovacionDuplicadaSeRechazaConElMensajeDeConflicto() {
        service = crearServicio(Clock.fixed(Instant.parse("2026-11-05T15:00:00Z"), ZoneId.of("America/Lima")));
        contratoBase();
        when(contratos.existsByAnteriorIdAndEstadoNot(10L, EstadoContrato.ANULADO)).thenReturn(true);
        var ex=assertThrows(ReglaNegocioException.class,()->service.renovar(10L,new RenovarContratoRequestDTO(2L,List.of("D"),pagos())));
        assertEquals("El contrato ya tiene una renovación vigente",ex.getMessage()); verify(contratos,never()).saveAndFlush(any());
    }

    private List<PagoRequestDTO> pagos() {
        return List.of(new PagoRequestDTO("YAPE", new BigDecimal("16.00"), null));
    }

    private ContratoRequestDTO request() {
        return new ContratoRequestDTO(1L, 2L, LocalDate.parse("2026-11-02"), List.of("D"), pagos());
    }
}
