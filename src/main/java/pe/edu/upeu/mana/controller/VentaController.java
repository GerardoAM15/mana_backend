package pe.edu.upeu.mana.controller;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.enums.EstadoVenta;
import pe.edu.upeu.mana.service.service.VentaService;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.venta.PagosVentaRequestDTO;
import pe.edu.upeu.mana.dto.venta.VentaRequestDTO;
import pe.edu.upeu.mana.dto.venta.VentaResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final VentaService service;

    public VentaController(VentaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> crear(@Valid @RequestBody VentaRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PostMapping("/{id}/pagos")
    public VentaResponseDTO cobrar(@PathVariable Long id, @Valid @RequestBody PagosVentaRequestDTO r) {
        return service.cobrar(id, r);
    }

    @PatchMapping("/{id}/anular")
    public VentaResponseDTO anular(@PathVariable Long id, @Valid @RequestBody AnularRequestDTO r) {
        return service.anular(id, r);
    }

    @GetMapping("/{id}")
    public VentaResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public PaginaResponseDTO<VentaResponseDTO> listar(@RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) EstadoVenta estado, @RequestParam(required = false) Long cajaId, @RequestParam(required = false) Long localId, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio, @RequestParam(defaultValue = "id") String ordenarPor, @RequestParam(defaultValue = "desc") String direccion) {
        return service.listar(desde, hasta, estado, cajaId, localId, pagina, tamanio, ordenarPor, direccion);
    }
}
