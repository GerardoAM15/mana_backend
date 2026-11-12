package pe.edu.upeu.mana.service.impl;

import java.math.*;
import java.time.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.repository.ReporteRepository;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.exception.DatosInvalidosException;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.reporte.AsistenciaDiariaResponseDTO;
import pe.edu.upeu.mana.dto.reporte.ContratoPorVencerResponseDTO;
import pe.edu.upeu.mana.dto.reporte.GastoIngresoResponseDTO;
import pe.edu.upeu.mana.dto.reporte.IngresoPeriodoResponseDTO;
import pe.edu.upeu.mana.dto.reporte.PlatoConsumidoResponseDTO;

@Service
public class ReporteServiceImpl implements ReporteService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteServiceImpl.class);

    private final ReporteRepository reportes;

    private final ContextoLocalService contexto;

    private final Clock clock;

    public ReporteServiceImpl(ReporteRepository reportes, ContextoLocalService contexto, Clock clock) {
        this.reportes = reportes;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<IngresoPeriodoResponseDTO> ingresos(LocalDate desde, LocalDate hasta, Long local) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReporteServiceImpl.ingresos");
        try {
            rango(desde, hasta);
            return reportes.ingresos(contexto.localId(local), desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()).stream().map(r -> new IngresoPeriodoResponseDTO(r.getPeriodo(), dinero(r.getContratos()), dinero(r.getVentas()), dinero(r.getContratos().add(r.getVentas())))).toList();
        } finally {
            log.debug("Fin ReporteServiceImpl.ingresos | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<PlatoConsumidoResponseDTO> platos(LocalDate desde, LocalDate hasta, Long local, String servicio) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReporteServiceImpl.platos");
        try {
            rango(desde, hasta);
            return reportes.platos(contexto.localId(local), desde, hasta, servicio);
        } finally {
            log.debug("Fin ReporteServiceImpl.platos | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<ContratoPorVencerResponseDTO> porVencer(Integer dias, LocalDate desde, LocalDate hasta, Long local) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReporteServiceImpl.porVencer");
        try {
            if (dias < 0 || dias > 366)
                throw new DatosInvalidosException("Los días deben estar entre 0 y 366");
            if (desde != null && hasta != null)
                rango(desde, hasta);
            Long localId = contexto.localId(local);
            var hoy = LocalDate.now(clock);
            var inicio = desde == null || desde.isBefore(hoy) ? hoy : desde;
            var fin = hoy.plusDays(dias);
            if (hasta != null && hasta.isBefore(fin))
                fin = hasta;
            if (inicio.isAfter(fin))
                return List.of();
            return reportes.porVencer(localId, hoy, inicio, fin);
        } finally {
            log.debug("Fin ReporteServiceImpl.porVencer | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDiariaResponseDTO> asistencia(LocalDate desde, LocalDate hasta, Long local) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReporteServiceImpl.asistencia");
        try {
            rango(desde, hasta);
            return reportes.asistencia(contexto.localId(local), desde, hasta);
        } finally {
            log.debug("Fin ReporteServiceImpl.asistencia | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<GastoIngresoResponseDTO> gastos(LocalDate desde, LocalDate hasta, Long local, String agrupacion) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReporteServiceImpl.gastos");
        try {
            rango(desde, hasta);
            String formato = switch(agrupacion) {
                case "DIA" ->
                    "YYYY-MM-DD";
                case "MES" ->
                    "YYYY-MM";
                default ->
                    throw new DatosInvalidosException("La agrupación debe ser DIA o MES");
            };
            return reportes.gastos(contexto.localId(local), desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), formato).stream().map(r -> new GastoIngresoResponseDTO(r.getPeriodo(), dinero(r.getIngresos()), dinero(r.getGastos()), dinero(r.getIngresos().subtract(r.getGastos())))).toList();
        } finally {
            log.debug("Fin ReporteServiceImpl.gastos | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private void rango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta))
            throw new DatosInvalidosException("Indique un rango de fechas válido");
    }
}
