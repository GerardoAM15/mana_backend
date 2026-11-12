package pe.edu.upeu.mana.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.PlatoMapper;
import pe.edu.upeu.mana.service.service.PlatoService;
import pe.edu.upeu.mana.dto.menu.PlatoRequestDTO;
import pe.edu.upeu.mana.dto.menu.PlatoResponseDTO;

@Service
public class PlatoServiceImpl implements PlatoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlatoServiceImpl.class);

    private final PlatoRepository repository;

    private final PlatoMapper mapper;

    private final CategoriaPlatoRepository categorias;

    public PlatoServiceImpl(PlatoRepository repository, PlatoMapper mapper, CategoriaPlatoRepository categorias) {
        this.repository = repository;
        this.mapper = mapper;
        this.categorias = categorias;
    }

    @Transactional
    public PlatoResponseDTO crear(PlatoRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio PlatoServiceImpl.crear");
        try {
            var e = new Plato();
            validar(e, r);
            mapper.actualizar(e, r);
            repository.saveAndFlush(e);
            return mapper.toResponse(e);
        } finally {
            log.debug("Fin PlatoServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public PlatoResponseDTO actualizar(Long id, PlatoRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio PlatoServiceImpl.actualizar");
        try {
            var e = obtener(id);
            validar(e, r);
            mapper.actualizar(e, r);
            repository.flush();
            return mapper.toResponse(e);
        } finally {
            log.debug("Fin PlatoServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public PlatoResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio PlatoServiceImpl.buscar");
        try {
            return mapper.toResponse(obtener(id));
        } finally {
            log.debug("Fin PlatoServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<PlatoResponseDTO> listar() {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio PlatoServiceImpl.listar");
        try {
            return repository.findAll(Sort.by("id")).stream().map(mapper::toResponse).toList();
        } finally {
            log.debug("Fin PlatoServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio PlatoServiceImpl.eliminar");
        try {
            obtener(id).setActivo(false);
        } finally {
            log.debug("Fin PlatoServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private Plato obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Plato no encontrado"));
    }

    private void validar(Plato e, PlatoRequestDTO r) {
        if (!categorias.findById(r.getCategoria()).map(c -> Boolean.TRUE.equals(c.getActivo())).orElse(false))
            throw new ReglaNegocioException("Categoría inexistente o inactiva");
    }
}
