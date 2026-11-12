package pe.edu.upeu.mana.service;

import java.time.*;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.mana.entity.Contrato;
import pe.edu.upeu.mana.enums.EstadoContrato;
import static org.junit.jupiter.api.Assertions.*;

class EstadoContratoActualTest {

    private final EstadoContratoActual estados = new EstadoContratoActual(Clock.fixed(Instant.parse("2026-11-02T15:00:00Z"), ZoneId.of("America/Lima")));

    @Test
    void programadoQueEmpiezaHoySeMuestraActivoSinEsperarScheduler() {
        var c = contrato(EstadoContrato.PROGRAMADO, "2026-11-02", "2026-11-08");
        assertEquals(EstadoContrato.ACTIVO, estados.calcular(c));
        assertEquals(EstadoContrato.PROGRAMADO, c.getEstado());
    }

    @Test
    void vencidoSeMuestraFinalizadoSinEsperarScheduler() {
        assertEquals(EstadoContrato.FINALIZADO, estados.calcular(contrato(EstadoContrato.ACTIVO, "2026-10-26", "2026-11-01")));
        assertEquals(EstadoContrato.FINALIZADO, estados.calcular(contrato(EstadoContrato.PROGRAMADO, "2026-10-26", "2026-11-01")));
    }

    @Test
    void fechaFinEsInclusivaYLosAnuladosNoSeReactivan() {
        assertEquals(EstadoContrato.ACTIVO, estados.calcular(contrato(EstadoContrato.ACTIVO, "2026-10-27", "2026-11-02")));
        assertEquals(EstadoContrato.ANULADO, estados.calcular(contrato(EstadoContrato.ANULADO, "2026-11-02", "2026-11-08")));
    }

    private Contrato contrato(EstadoContrato estado, String inicio, String fin) {
        var c = new Contrato();
        c.setEstado(estado);
        c.setFechaInicio(LocalDate.parse(inicio));
        c.setFechaFin(LocalDate.parse(fin));
        return c;
    }
}
