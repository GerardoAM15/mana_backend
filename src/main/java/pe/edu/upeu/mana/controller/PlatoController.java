package pe.edu.upeu.mana.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.PlatoService;
import pe.edu.upeu.mana.dto.menu.PlatoRequestDTO;
import pe.edu.upeu.mana.dto.menu.PlatoResponseDTO;

@RestController
@RequestMapping("/api/v1/platos")
public class PlatoController {

    private final PlatoService service;

    public PlatoController(PlatoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PlatoResponseDTO> crear(@Valid @RequestBody PlatoRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public PlatoResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody PlatoRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping("/{id}")
    public PlatoResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public List<PlatoResponseDTO> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
