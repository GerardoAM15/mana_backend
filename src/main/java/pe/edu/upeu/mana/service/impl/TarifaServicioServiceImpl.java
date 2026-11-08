package pe.edu.upeu.mana.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.TarifaServicioMapper;
import pe.edu.upeu.mana.service.service.TarifaServicioService;
import pe.edu.upeu.mana.dto.menu.TarifaServicioRequestDTO;
import pe.edu.upeu.mana.dto.menu.TarifaServicioResponseDTO;

@Service
public class TarifaServicioServiceImpl implements TarifaServicioService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TarifaServicioServiceImpl.class);

    private final TarifaServicioRepository repository;

    private final TarifaServicioMapper mapper;

    private final ServicioRepository servicios;

    public TarifaServicioServiceImpl(TarifaServicioRepository repository, TarifaServicioMapper mapper, ServicioRepository servicios) {
        this.repository = repository;
        this.mapper = mapper;
        this.servicios = servicios;
    }

    @Transactional
    public TarifaServicioResponseDTO crear(TarifaServicioRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio TarifaServicioServiceImpl.crear");
        try {
            var e = new TarifaServicio();
            validar(e, r);
            mapper.actualizar(e, r);
            repository.saveAndFlush(e);
            return mapper.toResponse(e);
        } finally {
            log.debug("Fin TarifaServicioServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public TarifaServicioResponseDTO actualizar(Long id, TarifaServicioRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio TarifaServicioServiceImpl.actualizar");
        try {
            var e = obtener(id);
            validar(e, r);
            mapper.actualizar(e, r);
            repository.flush();
            return mapper.toResponse(e);
        } finally {
            log.debug("Fin TarifaServicioServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public TarifaServicioResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio TarifaServicioServiceImpl.buscar");
        try {
            return mapper.toResponse(obtener(id));
        } finally {
            log.debug("Fin TarifaServicioServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<TarifaServicioResponseDTO> listar() {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio TarifaServicioServiceImpl.listar");
        try {
            return repository.findAll(Sort.by("id")).stream().map(mapper::toResponse).toList();
        } finally {
            log.debug("Fin TarifaServicioServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio TarifaServicioServiceImpl.eliminar");
        try {
            obtener(id).setActivo(false);
        } finally {
            log.debug("Fin TarifaServicioServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private TarifaServicio obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("TarifaServicio no encontrado"));
    }

    private void validar(TarifaServicio e, TarifaServicioRequestDTO r) {
        if (!servicios.findById(r.getServicio()).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
            throw new ReglaNegocioException("Servicio inexistente o inactivo");
    }
}
