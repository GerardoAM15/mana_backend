package pe.edu.upeu.mana.service;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.mana.enums.PeriodoPlan;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraRacionesTest {

    private final CalculadoraRaciones calculadora = new CalculadoraRaciones();

    @Test
    void semanalDesdeLunes() {
        verificar("2026-11-02", PeriodoPlan.SEMANAL, Set.of(), 5, 5, 4, "2026-11-08");
    }

    @Test
    void semanalDesdeMiercoles() {
        verificar("2026-11-04", PeriodoPlan.SEMANAL, Set.of(), 5, 5, 4, "2026-11-10");
    }

    @Test
    void mensualDesdeNoviembre() {
        verificar("2026-11-02", PeriodoPlan.MENSUAL, Set.of(), 22, 22, 18, "2026-12-01");
    }

    @Test
    void mensualDiciembre() {
        verificar("2026-12-01", PeriodoPlan.MENSUAL, Set.of(), 23, 23, 19, "2026-12-31");
    }

    @Test
    void mensualDiciembreConFeriado() {
        verificar("2026-12-01", PeriodoPlan.MENSUAL, Set.of(LocalDate.parse("2026-12-08")), 22, 22, 18, "2026-12-31");
    }

    private void verificar(String inicio, PeriodoPlan periodo, Set<LocalDate> feriados, long d, long a, long c, String finEsperado) {
        var desde = LocalDate.parse(inicio);
        var fin = calculadora.fechaFin(desde, periodo);
        assertEquals(LocalDate.parse(finEsperado), fin);
        assertEquals(d, calculadora.calcular(desde, fin, Set.of(1, 2, 3, 4, 5), feriados));
        assertEquals(a, calculadora.calcular(desde, fin, Set.of(1, 2, 3, 4, 5), feriados));
        assertEquals(c, calculadora.calcular(desde, fin, Set.of(1, 2, 3, 4), feriados));
    }
}
