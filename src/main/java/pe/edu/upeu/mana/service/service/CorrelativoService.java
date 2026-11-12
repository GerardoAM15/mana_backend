package pe.edu.upeu.mana.service.service;

import pe.edu.upeu.mana.enums.TipoCorrelativo;

public interface CorrelativoService {

    String siguiente(Long localId, TipoCorrelativo tipo);
}
