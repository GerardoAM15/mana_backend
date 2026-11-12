package pe.edu.upeu.mana.controller;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.CajaService;
import pe.edu.upeu.mana.dto.caja.AbrirCajaRequestDTO;
import pe.edu.upeu.mana.dto.caja.CajaResponseDTO;
import pe.edu.upeu.mana.dto.caja.CerrarCajaRequestDTO;
import pe.edu.upeu.mana.dto.caja.ResumenCajaResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@RestController
@RequestMapping("/api/v1/cajas")
public class CajaController {

    private final CajaService service;

    public CajaController(CajaService service) {
        this.service = service;
    }

    @PostMapping("/abrir")
    public ResponseEntity<CajaResponseDTO> abrir(@Valid @RequestBody AbrirCajaRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(r));
    }

    @GetMapping("/actual")
    public CajaResponseDTO buscarActual() {
        return service.buscarActual();
    }

    @GetMapping("/{id}/resumen")
    public ResumenCajaResponseDTO resumir(@PathVariable Long id) {
        return service.resumir(id);
    }

    @PostMapping("/{id}/cerrar")
    public CajaResponseDTO cerrar(@PathVariable Long id, @Valid @RequestBody CerrarCajaRequestDTO r) {
        return service.cerrar(id, r);
    }

    @GetMapping
    public PaginaResponseDTO<CajaResponseDTO> listar(@RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) Long usuarioId, @RequestParam(required = false) Long localId, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio, @RequestParam(defaultValue = "id") String ordenarPor, @RequestParam(defaultValue = "desc") String direccion) {
        return service.listar(desde, hasta, usuarioId, localId, pagina, tamanio, ordenarPor, direccion);
    }
}
