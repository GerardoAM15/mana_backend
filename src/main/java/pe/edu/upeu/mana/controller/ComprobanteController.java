package pe.edu.upeu.mana.controller;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.dto.comprobante.*;
import pe.edu.upeu.mana.enums.*;
import pe.edu.upeu.mana.service.service.ComprobanteService;

@RestController
@RequestMapping("/api/v1/comprobantes")
public class ComprobanteController {
    private final ComprobanteService service;
    public ComprobanteController(ComprobanteService service) { this.service=service; }
    @GetMapping
    public PaginaResponseDTO<ComprobanteResponseDTO> listar(@RequestParam(required=false) LocalDate desde,@RequestParam(required=false) LocalDate hasta,
        @RequestParam(required=false) TipoComprobante tipo,@RequestParam(required=false) EstadoComprobante estado,@RequestParam(required=false) Long localId,
        @RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanio) { return service.listar(desde,hasta,tipo,estado,localId,pagina,tamanio); }
    @GetMapping("/configuracion") public ComprobanteConfiguracionDTO configuracion() { return service.configuracion(); }
    @GetMapping("/{id}") public ComprobanteResponseDTO buscar(@PathVariable Long id) { return service.buscar(id); }
    @GetMapping(value="/{id}/pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) { return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(service.pdf(id)); }
    @GetMapping(value="/{id}/xml",produces=MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> xml(@PathVariable Long id) { return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(service.xml(id).getBytes(StandardCharsets.UTF_8)); }
    @PostMapping("/{id}/reenviar") public ComprobanteResponseDTO reenviar(@PathVariable Long id) { return service.reenviar(id); }
    // Envía el PDF y el XML por correo (COMPROBANTE_EMITIR). Body opcional: { "email": "cliente@correo.com" }.
    @PostMapping("/{id}/correo") public ComprobanteResponseDTO correo(@PathVariable Long id,@Valid @RequestBody(required=false) CorreoComprobanteRequestDTO r) {
        return service.enviarCorreo(id,r==null?null:r.email()); }
    @GetMapping("/ventas/{ventaId}") public ComprobanteResponseDTO venta(@PathVariable Long ventaId) { return service.venta(ventaId); }
    @GetMapping("/contratos/{contratoId}") public ComprobanteResponseDTO contrato(@PathVariable Long contratoId) { return service.contrato(contratoId); }
    @PostMapping("/ventas/{ventaId}") public ResponseEntity<ComprobanteResponseDTO> emitirVenta(@PathVariable Long ventaId,@Valid @RequestBody ComprobanteRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.emitirVenta(ventaId,r)); }
    @PostMapping("/contratos/{contratoId}") public ResponseEntity<ComprobanteResponseDTO> emitirContrato(@PathVariable Long contratoId,@Valid @RequestBody ComprobanteRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.emitirContrato(contratoId,r)); }
}
