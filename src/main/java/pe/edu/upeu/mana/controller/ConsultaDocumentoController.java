package pe.edu.upeu.mana.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.dto.consulta.DniResponseDTO;
import pe.edu.upeu.mana.dto.consulta.RucResponseDTO;
import pe.edu.upeu.mana.service.service.ConsultaDocumentoService;

// Autocompletado de formularios por DNI (clientes y usuarios) y RUC (proveedores).
@RestController
@RequestMapping("/api/v1/consultas")
public class ConsultaDocumentoController {

    private final ConsultaDocumentoService service;

    public ConsultaDocumentoController(ConsultaDocumentoService service) {
        this.service = service;
    }

    @GetMapping("/dni/{dni}")
    public DniResponseDTO dni(@PathVariable String dni) {
        return service.consultarDni(dni);
    }

    @GetMapping("/ruc/{ruc}")
    public RucResponseDTO ruc(@PathVariable String ruc) {
        return service.consultarRuc(ruc);
    }
}
