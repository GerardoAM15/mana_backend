package pe.edu.upeu.mana.service.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import pe.edu.upeu.mana.entity.SesionCaja;
import pe.edu.upeu.mana.dto.caja.AbrirCajaRequestDTO;
import pe.edu.upeu.mana.dto.caja.CajaResponseDTO;
import pe.edu.upeu.mana.dto.caja.CerrarCajaRequestDTO;
import pe.edu.upeu.mana.dto.caja.ResumenCajaResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

public interface CajaService {

    CajaResponseDTO abrir(AbrirCajaRequestDTO request);

    CajaResponseDTO buscarActual();

    ResumenCajaResponseDTO resumir(Long id);

    CajaResponseDTO cerrar(Long id, CerrarCajaRequestDTO request);

    PaginaResponseDTO<CajaResponseDTO> listar(LocalDate desde, LocalDate hasta, Long usuarioId, Long localId, int pagina, int tamanio, String ordenarPor, String direccion);

    SesionCaja buscarActualParaActualizar();

    SesionCaja buscarAbiertaParaActualizar(Long id);

    void validarDevolucion(Long cajaId, Map<String, BigDecimal> montosPorMetodo);
}
