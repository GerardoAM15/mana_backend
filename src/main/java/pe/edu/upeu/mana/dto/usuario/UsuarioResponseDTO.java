package pe.edu.upeu.mana.dto.usuario;

import pe.edu.upeu.mana.enums.RolUsuario;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;

    private Long empleadoId;

    private String dni;

    private String nombre;

    private String apellido;

    private String telefono;

    private Long localId;

    private String localNombre;

    private String cargo;

    private String correo;

    private RolUsuario rol;

    private Boolean activo;

    private Long version;
}
