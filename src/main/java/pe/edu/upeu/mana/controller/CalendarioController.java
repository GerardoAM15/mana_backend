package pe.edu.upeu.mana.controller;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.CalendarioService;
import pe.edu.upeu.mana.dto.calendario.DiaNoAtencionRequestDTO;
import pe.edu.upeu.mana.dto.calendario.DiaNoAtencionResponseDTO;

@RestController
@RequestMapping("/api/v1/dias-no-atencion")
public class CalendarioController {

    private final CalendarioService service;

    public CalendarioController(CalendarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DiaNoAtencionResponseDTO> crear(@Valid @RequestBody DiaNoAtencionRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public DiaNoAtencionResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody DiaNoAtencionRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping
    public List<DiaNoAtencionResponseDTO> listar(@RequestParam(required = false) Long localId, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta) {
        return service.listar(localId, desde, hasta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
