package pe.edu.upeu.mana.dto.consulta;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Datos de SUNAT para autocompletar un proveedor. estado (ACTIVO...) y
// condicion (HABIDO...) son informativos: no se guardan.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RucResponseDTO {

    private String ruc;

    private String razonSocial;

    private String direccion;

    private String telefono;

    private String estado;

    private String condicion;
}
