package pe.edu.upeu.mana.dto.usuario;

import jakarta.validation.constraints.*;
import pe.edu.upeu.mana.enums.RolUsuario;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "[0-9]{8}", message = "El DNI debe tener 8 dígitos")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String apellido;

    @Size(max = 20, message = "Máximo 20 caracteres")
    private String telefono;

    @NotNull(message = "El local es obligatorio")
    @Positive(message = "Local inválido")
    private Long localId;

    @NotBlank(message = "El cargo es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String cargo;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    @Size(max = 160, message = "Máximo 160 caracteres")
    private String correo;

    @Size(min = 8, max = 72, message = "La clave debe tener entre 8 y 72 caracteres")
    private String clave;

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}
