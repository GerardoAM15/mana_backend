package pe.edu.upeu.mana.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.ContratoService;
import pe.edu.upeu.mana.dto.contrato.ContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.PensionistaResponseDTO;

@RestController
@RequestMapping("/api/v1")
public class PensionistaController {

    private final ContratoService service;

    public PensionistaController(ContratoService service) {
        this.service = service;
    }

    @GetMapping("/pensionistas/buscar")
    public List<PensionistaResponseDTO> buscar(@RequestParam String q, @RequestParam(required = false) Long localId) {
        return service.buscarPensionistas(q, localId);
    }

    @GetMapping("/clientes/{id}/contrato-vigente")
    public ContratoResponseDTO buscarVigente(@PathVariable Long id, @RequestParam(required = false) Long localId) {
        return service.buscarVigente(id, localId);
    }
}
