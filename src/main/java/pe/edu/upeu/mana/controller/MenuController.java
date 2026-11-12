package pe.edu.upeu.mana.controller;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.service.service.MenuService;
import pe.edu.upeu.mana.dto.menu.ClonarMenuRequestDTO;
import pe.edu.upeu.mana.dto.menu.CopiarCenaRequestDTO;
import pe.edu.upeu.mana.dto.menu.MenuRequestDTO;
import pe.edu.upeu.mana.dto.menu.MenuResponseDTO;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService service;

    public MenuController(MenuService service) {
        this.service = service;
    }

    @PutMapping
    public MenuResponseDTO guardar(@Valid @RequestBody MenuRequestDTO r) {
        return service.guardar(r);
    }

    @GetMapping
    public List<MenuResponseDTO> listar(@RequestParam(required = false) Long localId, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) String servicio) {
        return service.listar(localId, desde, hasta, servicio);
    }

    @PostMapping("/clonar")
    public List<MenuResponseDTO> clonar(@Valid @RequestBody ClonarMenuRequestDTO r) {
        return service.clonar(r);
    }

    @PostMapping("/copiar-a-cena")
    public MenuResponseDTO copiarACena(@Valid @RequestBody CopiarCenaRequestDTO r) {
        return service.copiarACena(r);
    }
}
