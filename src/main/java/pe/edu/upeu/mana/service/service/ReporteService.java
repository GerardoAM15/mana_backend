package pe.edu.upeu.mana.service.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.mana.dto.reporte.AsistenciaDiariaResponseDTO;
import pe.edu.upeu.mana.dto.reporte.ContratoPorVencerResponseDTO;
import pe.edu.upeu.mana.dto.reporte.GastoIngresoResponseDTO;
import pe.edu.upeu.mana.dto.reporte.IngresoPeriodoResponseDTO;
import pe.edu.upeu.mana.dto.reporte.PlatoConsumidoResponseDTO;

public interface ReporteService {

    List<IngresoPeriodoResponseDTO> ingresos(LocalDate desde, LocalDate hasta, Long localId);

    List<PlatoConsumidoResponseDTO> platos(LocalDate desde, LocalDate hasta, Long localId, String servicio);

    List<ContratoPorVencerResponseDTO> porVencer(Integer dias, LocalDate desde, LocalDate hasta, Long localId);

    List<AsistenciaDiariaResponseDTO> asistencia(LocalDate desde, LocalDate hasta, Long localId);

    List<GastoIngresoResponseDTO> gastos(LocalDate desde, LocalDate hasta, Long localId, String agrupacion);
}
