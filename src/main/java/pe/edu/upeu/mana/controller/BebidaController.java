package pe.edu.upeu.mana.controller;

import java.util.List;
import java.time.LocalDate;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.BebidaService;
import pe.edu.upeu.mana.dto.bebida.BebidaRequestDTO;
import pe.edu.upeu.mana.dto.bebida.BebidaResponseDTO;

@RestController
@RequestMapping("/api/v1/bebidas")
public class BebidaController {

    private final BebidaService service;

    public BebidaController(BebidaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BebidaResponseDTO> crear(@Valid @RequestBody BebidaRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public BebidaResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody BebidaRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping("/{id}")
    public BebidaResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public List<BebidaResponseDTO> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
