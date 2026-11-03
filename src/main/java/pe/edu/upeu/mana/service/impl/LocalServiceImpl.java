package pe.edu.upeu.mana.service.impl;

import pe.edu.upeu.mana.enums.TipoCorrelativo;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.LocalMapper;
import pe.edu.upeu.mana.service.service.LocalService;
import pe.edu.upeu.mana.dto.local.LocalRequestDTO;
import pe.edu.upeu.mana.dto.local.LocalResponseDTO;

@Service
public class LocalServiceImpl implements LocalService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalServiceImpl.class);

    private final LocalRepository repository;

    private final LocalMapper mapper;

    private final CorrelativoRepository correlativos;

    public LocalServiceImpl(LocalRepository repository, LocalMapper mapper, CorrelativoRepository correlativos) {
        this.repository = repository;
        this.mapper = mapper;
        this.correlativos = correlativos;
    }

    @Transactional
    public LocalResponseDTO crear(LocalRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio LocalServiceImpl.crear");
        try {
            var e = new Local();
            validar(e, r);
            mapper.actualizar(e, r);
            repository.saveAndFlush(e);
            for (var tipo : TipoCorrelativo.values()) {
                var c = new Correlativo();
                c.setLocalId(e.getId());
                c.setTipo(tipo);
                c.setUltimoNumero(0L);
                correlativos.save(c);
            }
            return mapper.toResponse(e);
        } finally {
            log.info("Fin LocalServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public LocalResponseDTO actualizar(Long id, LocalRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio LocalServiceImpl.actualizar");
        try {
            var e = obtener(id);
            validar(e, r);
            mapper.actualizar(e, r);
            repository.flush();
            return mapper.toResponse(e);
        } finally {
            log.info("Fin LocalServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public LocalResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio LocalServiceImpl.buscar");
        try {
            return mapper.toResponse(obtener(id));
        } finally {
            log.info("Fin LocalServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<LocalResponseDTO> listar() {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio LocalServiceImpl.listar");
        try {
            return repository.findAll(Sort.by("id")).stream().map(mapper::toResponse).toList();
        } finally {
            log.info("Fin LocalServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio LocalServiceImpl.eliminar");
        try {
            obtener(id).setActivo(false);
        } finally {
            log.info("Fin LocalServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private Local obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Local no encontrado"));
    }

    private void validar(Local e, LocalRequestDTO r) {
    }
}
