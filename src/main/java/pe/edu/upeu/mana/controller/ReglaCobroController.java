package pe.edu.upeu.mana.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.ReglaCobroService;
import pe.edu.upeu.mana.dto.menu.ReglaCobroRequestDTO;
import pe.edu.upeu.mana.dto.menu.ReglaCobroResponseDTO;

@RestController
@RequestMapping("/api/v1/reglas-cobro")
public class ReglaCobroController {

    private final ReglaCobroService service;

    public ReglaCobroController(ReglaCobroService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReglaCobroResponseDTO> crear(@Valid @RequestBody ReglaCobroRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public ReglaCobroResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody ReglaCobroRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping("/{id}")
    public ReglaCobroResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public List<ReglaCobroResponseDTO> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
