package pe.edu.upeu.mana.service.service;

import pe.edu.upeu.mana.dto.consulta.DniResponseDTO;
import pe.edu.upeu.mana.dto.consulta.RucResponseDTO;

public interface ConsultaDocumentoService {

    DniResponseDTO consultarDni(String dni);

    RucResponseDTO consultarRuc(String ruc);
}
