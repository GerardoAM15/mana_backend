package pe.edu.upeu.mana.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.ReporteService;
import pe.edu.upeu.mana.dto.reporte.AsistenciaDiariaResponseDTO;
import pe.edu.upeu.mana.dto.reporte.ContratoPorVencerResponseDTO;
import pe.edu.upeu.mana.dto.reporte.GastoIngresoResponseDTO;
import pe.edu.upeu.mana.dto.reporte.IngresoPeriodoResponseDTO;
import pe.edu.upeu.mana.dto.reporte.PlatoConsumidoResponseDTO;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping("/ingresos-mensuales")
    public List<IngresoPeriodoResponseDTO> ingresos(@RequestParam LocalDate desde, @RequestParam LocalDate hasta, @RequestParam(required = false) Long localId) {
        return service.ingresos(desde, hasta, localId);
    }

    @GetMapping("/ventas-vs-pensiones")
    public List<IngresoPeriodoResponseDTO> comparar(@RequestParam LocalDate desde, @RequestParam LocalDate hasta, @RequestParam(required = false) Long localId) {
        return service.ingresos(desde, hasta, localId);
    }

    @GetMapping("/platos-mas-consumidos")
    public List<PlatoConsumidoResponseDTO> platos(@RequestParam LocalDate desde, @RequestParam LocalDate hasta, @RequestParam(required = false) Long localId, @RequestParam(required = false) String servicio) {
        return service.platos(desde, hasta, localId, servicio);
    }

    @GetMapping("/contratos-por-vencer")
    public List<ContratoPorVencerResponseDTO> vencer(@RequestParam(defaultValue = "3") Integer dias, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) Long localId) {
        return service.porVencer(dias, desde, hasta, localId);
    }

    @GetMapping("/asistencia-diaria")
    public List<AsistenciaDiariaResponseDTO> asistencia(@RequestParam LocalDate desde, @RequestParam LocalDate hasta, @RequestParam(required = false) Long localId) {
        return service.asistencia(desde, hasta, localId);
    }

    @GetMapping("/gastos-vs-ingresos")
    public List<GastoIngresoResponseDTO> gastos(@RequestParam LocalDate desde, @RequestParam LocalDate hasta, @RequestParam(required = false) Long localId, @RequestParam(defaultValue = "DIA") String agrupacion) {
        return service.gastos(desde, hasta, localId, agrupacion);
    }
}
