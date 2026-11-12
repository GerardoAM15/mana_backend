package pe.edu.upeu.mana.controller;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.InventarioService;
import pe.edu.upeu.mana.dto.inventario.CompraBebidaRequestDTO;
import pe.edu.upeu.mana.dto.inventario.CompraBebidaResponseDTO;
import pe.edu.upeu.mana.dto.inventario.MovimientoStockResponseDTO;
import pe.edu.upeu.mana.dto.inventario.StockBebidaResponseDTO;

@RestController
@RequestMapping("/api/v1")
public class InventarioController {

    private final InventarioService service;

    public InventarioController(InventarioService service) {
        this.service = service;
    }

    @PostMapping("/compras-bebidas")
    public ResponseEntity<CompraBebidaResponseDTO> crear(@Valid @RequestBody CompraBebidaRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @GetMapping("/compras-bebidas/{id}")
    public CompraBebidaResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/compras-bebidas")
    public List<CompraBebidaResponseDTO> listar(@RequestParam(required = false) Long localId, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta) {
        return service.listar(localId, desde, hasta);
    }

    @GetMapping("/stock-bebidas")
    public List<StockBebidaResponseDTO> stock(@RequestParam(required = false) Long localId) {
        return service.listarStock(localId);
    }

    @GetMapping("/movimientos-stock")
    public pe.edu.upeu.mana.dto.PaginaResponseDTO<MovimientoStockResponseDTO> movimientos(@RequestParam(required = false) Long localId, @RequestParam(required = false) Long bebidaId, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio, @RequestParam(defaultValue = "id") String ordenarPor, @RequestParam(defaultValue = "desc") String direccion) {
        return service.listarMovimientos(localId, bebidaId, desde, hasta, pagina, tamanio, ordenarPor, direccion);
    }
}
