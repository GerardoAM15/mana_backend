package pe.edu.upeu.mana.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.PrecioSueltoService;
import pe.edu.upeu.mana.dto.menu.PrecioSueltoRequestDTO;
import pe.edu.upeu.mana.dto.menu.PrecioSueltoResponseDTO;

@RestController
@RequestMapping("/api/v1/precios-sueltos")
public class PrecioSueltoController {

    private final PrecioSueltoService service;

    public PrecioSueltoController(PrecioSueltoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PrecioSueltoResponseDTO> crear(@Valid @RequestBody PrecioSueltoRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{servicio}/{categoria}")
    public PrecioSueltoResponseDTO actualizar(@PathVariable String servicio, @PathVariable String categoria, @Valid @RequestBody PrecioSueltoRequestDTO r) {
        return service.actualizar(servicio, categoria, r);
    }

    @GetMapping
    public List<PrecioSueltoResponseDTO> listar() {
        return service.listar();
    }

    @DeleteMapping("/{servicio}/{categoria}")
    public ResponseEntity<Void> eliminar(@PathVariable String servicio, @PathVariable String categoria) {
        service.eliminar(servicio, categoria);
        return ResponseEntity.noContent().build();
    }
}
