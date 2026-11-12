package pe.edu.upeu.mana.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.ConsumoService;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.consumo.ConsumoRequestDTO;
import pe.edu.upeu.mana.dto.consumo.ConsumoResponseDTO;

@RestController
@RequestMapping("/api/v1/consumos")
public class ConsumoController {

    private final ConsumoService service;

    public ConsumoController(ConsumoService service) {
        this.service = service;
    }

    @GetMapping
    public pe.edu.upeu.mana.dto.PaginaResponseDTO<ConsumoResponseDTO> listar(@RequestParam(required = false) Long contratoId, @RequestParam(required = false) String servicio, @RequestParam(required = false) java.time.LocalDate desde, @RequestParam(required = false) java.time.LocalDate hasta, @RequestParam(required = false) Long localId, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio, @RequestParam(defaultValue = "id") String ordenarPor, @RequestParam(defaultValue = "desc") String direccion) {
        return service.listar(contratoId, servicio, desde, hasta, localId, pagina, tamanio, ordenarPor, direccion);
    }

    @PostMapping
    public ResponseEntity<ConsumoResponseDTO> crear(@Valid @RequestBody ConsumoRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PatchMapping("/{id}/anular")
    public ConsumoResponseDTO anular(@PathVariable Long id, @Valid @RequestBody AnularRequestDTO r) {
        return service.anular(id, r);
    }
}
