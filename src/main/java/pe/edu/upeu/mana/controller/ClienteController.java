package pe.edu.upeu.mana.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.ClienteService;
import pe.edu.upeu.mana.dto.cliente.ClienteRequestDTO;
import pe.edu.upeu.mana.dto.cliente.ClienteResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public ClienteResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO r) {
        return service.actualizar(id, r);
    }

    @GetMapping("/{id}")
    public ClienteResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public PaginaResponseDTO<ClienteResponseDTO> listar(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio, @RequestParam(defaultValue = "id") String ordenarPor, @RequestParam(defaultValue = "desc") String direccion) {
        return service.listar(q, pagina, tamanio, ordenarPor, direccion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
