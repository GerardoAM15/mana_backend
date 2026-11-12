package pe.edu.upeu.mana.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import pe.edu.upeu.mana.enums.TipoCorrelativo;
import pe.edu.upeu.mana.repository.CorrelativoRepository;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.service.service.CorrelativoService;

@Service
public class CorrelativoServiceImpl implements CorrelativoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CorrelativoServiceImpl.class);

    private final CorrelativoRepository correlativos;

    public CorrelativoServiceImpl(CorrelativoRepository correlativos) {
        this.correlativos = correlativos;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String siguiente(Long localId, TipoCorrelativo tipo) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio CorrelativoServiceImpl.siguiente");
        try {
            var c = correlativos.buscarParaActualizar(localId, tipo).orElseThrow(() -> new ReglaNegocioException("Falta configurar el correlativo del local"));
            if (c.getUltimoNumero() >= 9999999999L)
                throw new ReglaNegocioException("Se agotó el correlativo del local");
            c.setUltimoNumero(c.getUltimoNumero() + 1);
            return (tipo == TipoCorrelativo.CONTRATO ? "C-" : "V-") + "%06d".formatted(c.getUltimoNumero());
        } finally {
            log.debug("Fin CorrelativoServiceImpl.siguiente | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
