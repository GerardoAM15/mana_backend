package pe.edu.upeu.mana.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.LocalService;
import pe.edu.upeu.mana.dto.local.LocalRequestDTO;
import pe.edu.upeu.mana.dto.local.LocalResponseDTO;

@RestController
@RequestMapping("/api/v1/locales")
public class LocalController {

    private final LocalService service;

    public LocalController(LocalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<LocalResponseDTO> crear(@Valid @RequestBody LocalRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public LocalResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody LocalRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping("/{id}")
    public LocalResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public List<LocalResponseDTO> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
