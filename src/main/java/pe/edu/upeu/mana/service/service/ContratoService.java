package pe.edu.upeu.mana.service.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.mana.enums.EstadoContrato;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoRequestDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.PensionistaResponseDTO;
import pe.edu.upeu.mana.dto.contrato.RenovarContratoRequestDTO;
import pe.edu.upeu.mana.dto.contrato.SimulacionContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.SimularContratoRequestDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

public interface ContratoService {

    SimulacionContratoResponseDTO simular(SimularContratoRequestDTO r);

    ContratoResponseDTO crear(ContratoRequestDTO r);

    ContratoResponseDTO renovar(Long id, RenovarContratoRequestDTO r);

    ContratoResponseDTO anular(Long id, AnularRequestDTO r);

    ContratoResponseDTO buscar(Long id);

    PaginaResponseDTO<ContratoResponseDTO> listar(String dni, EstadoContrato estado, LocalDate desde, LocalDate hasta, Long localId, int pagina, int tamanio, String ordenarPor, String direccion);

    ContratoResponseDTO buscarVigente(Long clienteId, Long localId);

    List<PensionistaResponseDTO> buscarPensionistas(String q, Long localId);

    void actualizarEstados();
}
