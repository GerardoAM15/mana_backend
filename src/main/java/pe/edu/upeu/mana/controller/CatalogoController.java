package pe.edu.upeu.mana.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.dto.catalogo.CatalogosResponseDTO;
import pe.edu.upeu.mana.service.service.CatalogoService;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoController {

    private final CatalogoService service;

    public CatalogoController(CatalogoService service) {
        this.service = service;
    }

    @GetMapping
    public CatalogosResponseDTO listar() {
        return service.listar();
    }
}
