package pe.edu.upeu.mana.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.PlanService;
import pe.edu.upeu.mana.dto.plan.PlanRequestDTO;
import pe.edu.upeu.mana.dto.plan.PlanResponseDTO;

@RestController
@RequestMapping("/api/v1/planes")
public class PlanController {

    private final PlanService service;

    public PlanController(PlanService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PlanResponseDTO> crear(@Valid @RequestBody PlanRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public PlanResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody PlanRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping("/{id}")
    public PlanResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public List<PlanResponseDTO> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
