package pe.edu.upeu.mana.service.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.mana.enums.EstadoVenta;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.venta.PagosVentaRequestDTO;
import pe.edu.upeu.mana.dto.pos.PosCatalogoResponseDTO;
import pe.edu.upeu.mana.dto.venta.VentaRequestDTO;
import pe.edu.upeu.mana.dto.venta.VentaResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

public interface VentaService {

    VentaResponseDTO crear(VentaRequestDTO r);

    VentaResponseDTO cobrar(Long id, PagosVentaRequestDTO r);

    VentaResponseDTO anular(Long id, AnularRequestDTO r);

    VentaResponseDTO buscar(Long id);

    PaginaResponseDTO<VentaResponseDTO> listar(LocalDate desde, LocalDate hasta, EstadoVenta estado, Long cajaId, Long localId, int pagina, int tamanio, String ordenarPor, String direccion);

    PosCatalogoResponseDTO catalogo(String servicio);
}
