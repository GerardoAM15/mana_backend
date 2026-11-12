package pe.edu.upeu.mana.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.ProveedorService;
import pe.edu.upeu.mana.dto.proveedor.ProveedorRequestDTO;
import pe.edu.upeu.mana.dto.proveedor.ProveedorResponseDTO;

@RestController
@RequestMapping("/api/v1/proveedores")
public class ProveedorController {

    private final ProveedorService service;

    public ProveedorController(ProveedorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProveedorResponseDTO> crear(@Valid @RequestBody ProveedorRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public ProveedorResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping("/{id}")
    public ProveedorResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public List<ProveedorResponseDTO> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
