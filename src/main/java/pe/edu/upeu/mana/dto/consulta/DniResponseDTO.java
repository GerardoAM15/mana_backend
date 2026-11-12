package pe.edu.upeu.mana.dto.consulta;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Datos para autocompletar un formulario a partir del DNI.
// fuente = MANA si la persona ya está registrada; RENIEC si vino de APIsPERU.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DniResponseDTO {

    private String dni;

    private String nombre;

    private String apellido;

    private String fuente;

    private boolean registrado;
}
