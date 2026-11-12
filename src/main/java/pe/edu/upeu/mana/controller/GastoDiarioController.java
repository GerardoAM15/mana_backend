package pe.edu.upeu.mana.controller;

import java.util.List;
import java.time.LocalDate;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.GastoDiarioService;
import pe.edu.upeu.mana.dto.gasto.GastoDiarioRequestDTO;
import pe.edu.upeu.mana.dto.gasto.GastoDiarioResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@RestController
@RequestMapping("/api/v1/gastos")
public class GastoDiarioController {

    private final GastoDiarioService service;

    public GastoDiarioController(GastoDiarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GastoDiarioResponseDTO> crear(@Valid @RequestBody GastoDiarioRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public GastoDiarioResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody GastoDiarioRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping("/{id}")
    public GastoDiarioResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public PaginaResponseDTO<GastoDiarioResponseDTO> listar(@RequestParam(required = false) Long localId, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio, @RequestParam(defaultValue = "id") String ordenarPor, @RequestParam(defaultValue = "desc") String direccion) {
        return service.listar(localId, desde, hasta, pagina, tamanio, ordenarPor, direccion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
