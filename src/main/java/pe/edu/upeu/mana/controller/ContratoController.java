package pe.edu.upeu.mana.controller;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.enums.EstadoContrato;
import pe.edu.upeu.mana.service.service.ContratoService;
import pe.edu.upeu.mana.dto.comun.AnularRequestDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoRequestDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.RenovarContratoRequestDTO;
import pe.edu.upeu.mana.dto.contrato.SimulacionContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.SimularContratoRequestDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@RestController
@RequestMapping("/api/v1/contratos")
public class ContratoController {

    private final ContratoService service;

    public ContratoController(ContratoService service) {
        this.service = service;
    }

    @PostMapping("/simular")
    public SimulacionContratoResponseDTO simular(@Valid @RequestBody SimularContratoRequestDTO r) {
        return service.simular(r);
    }

    @PostMapping
    public ResponseEntity<ContratoResponseDTO> crear(@Valid @RequestBody ContratoRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PostMapping("/{id}/renovar")
    public ResponseEntity<ContratoResponseDTO> renovar(@PathVariable Long id, @Valid @RequestBody RenovarContratoRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.renovar(id, r));
    }

    @PatchMapping("/{id}/anular")
    public ContratoResponseDTO anular(@PathVariable Long id, @Valid @RequestBody AnularRequestDTO r) {
        return service.anular(id, r);
    }

    @GetMapping("/{id}")
    public ContratoResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public PaginaResponseDTO<ContratoResponseDTO> listar(@RequestParam(required = false) String dni, @RequestParam(required = false) EstadoContrato estado, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) Long localId, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio, @RequestParam(defaultValue = "id") String ordenarPor, @RequestParam(defaultValue = "desc") String direccion) {
        return service.listar(dni, estado, desde, hasta, localId, pagina, tamanio, ordenarPor, direccion);
    }
}
