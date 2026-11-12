package pe.edu.upeu.mana.service.service;

import pe.edu.upeu.mana.entity.Usuario;

public interface ContextoLocalService {

    Usuario usuario();

    Long localId(Long filtro);

    void validarLocal(Long localId);
}
