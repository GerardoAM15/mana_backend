package pe.edu.upeu.mana.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.dto.pos.PosCatalogoResponseDTO;
import pe.edu.upeu.mana.service.service.VentaService;

@RestController
@RequestMapping("/api/v1/pos")
public class PosController {

    private final VentaService service;

    public PosController(VentaService service) {
        this.service = service;
    }

    @GetMapping("/catalogo")
    public PosCatalogoResponseDTO catalogo(@RequestParam String servicio) {
        return service.catalogo(servicio);
    }
}
