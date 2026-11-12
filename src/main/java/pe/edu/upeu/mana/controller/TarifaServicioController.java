package pe.edu.upeu.mana.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.TarifaServicioService;
import pe.edu.upeu.mana.dto.menu.TarifaServicioRequestDTO;
import pe.edu.upeu.mana.dto.menu.TarifaServicioResponseDTO;

@RestController
@RequestMapping("/api/v1/tarifas")
public class TarifaServicioController {

    private final TarifaServicioService service;

    public TarifaServicioController(TarifaServicioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TarifaServicioResponseDTO> crear(@Valid @RequestBody TarifaServicioRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public TarifaServicioResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody TarifaServicioRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping("/{id}")
    public TarifaServicioResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public List<TarifaServicioResponseDTO> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
