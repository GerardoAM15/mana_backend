package pe.edu.upeu.mana.service;

import java.time.LocalDate;
import java.util.Set;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.enums.PeriodoPlan;
import pe.edu.upeu.mana.exception.DatosInvalidosException;

@Component
public class CalculadoraRaciones {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CalculadoraRaciones.class);

    public LocalDate fechaFin(LocalDate inicio, PeriodoPlan periodo) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio CalculadoraRaciones.fechaFin");
        try {
            return switch(periodo) {
                case SEMANAL ->
                    inicio.plusDays(6);
                case MENSUAL ->
                    inicio.plusMonths(1).minusDays(1);
            };
        } finally {
            log.debug("Fin CalculadoraRaciones.fechaFin | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    public long calcular(LocalDate inicio, LocalDate fin, Set<Integer> diasAtencion, Set<LocalDate> cierres) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio CalculadoraRaciones.calcular");
        try {
            if (fin.isBefore(inicio))
                throw new DatosInvalidosException("Rango de fechas inválido");
            return inicio.datesUntil(fin.plusDays(1)).filter(d -> diasAtencion.contains(d.getDayOfWeek().getValue()) && !cierres.contains(d)).count();
        } finally {
            log.debug("Fin CalculadoraRaciones.calcular | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
