package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.SesionCaja;
import pe.edu.upeu.mana.dto.caja.CajaResponseDTO;

@Component
public class CajaMapper {

    public CajaResponseDTO toResponse(SesionCaja e) {
        return new CajaResponseDTO(e.getId(), e.getLocalId(), e.getUsuarioId(), e.getApertura(), e.getCierre(), e.getMontoInicial(), e.getMontoEsperado(), e.getMontoContado(), e.getDiferencia(), e.getObservacion(), e.getVersion());
    }
}
