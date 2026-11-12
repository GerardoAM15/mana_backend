package pe.edu.upeu.mana.dto.comprobante;
import jakarta.validation.constraints.*;
// POST /comprobantes/{id}/correo. Sin email se usa el guardado en el comprobante.
public record CorreoComprobanteRequestDTO(
    @Email(message = "Correo inválido") @Size(max = 120, message = "Correo de máximo 120 caracteres") String email) {}
