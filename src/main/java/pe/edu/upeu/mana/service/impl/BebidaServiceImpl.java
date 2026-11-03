package pe.edu.upeu.mana.service.impl;

import java.util.List;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.Bebida;
import pe.edu.upeu.mana.repository.BebidaRepository;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.mapper.BebidaMapper;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.bebida.BebidaRequestDTO;
import pe.edu.upeu.mana.dto.bebida.BebidaResponseDTO;

@Service
public class BebidaServiceImpl implements BebidaService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BebidaServiceImpl.class);

    private final BebidaRepository repository;

    private final BebidaMapper mapper;

    public BebidaServiceImpl(BebidaRepository repository, BebidaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public BebidaResponseDTO crear(BebidaRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio BebidaServiceImpl.crear");
        try {
            var e = new Bebida();
            mapper.actualizar(e, r);
            return mapper.toResponse(repository.saveAndFlush(e));
        } finally {
            log.info("Fin BebidaServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public BebidaResponseDTO actualizar(Long id, BebidaRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio BebidaServiceImpl.actualizar");
        try {
            var e = obtener(id);
            mapper.actualizar(e, r);
            repository.flush();
            return mapper.toResponse(e);
        } finally {
            log.info("Fin BebidaServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public BebidaResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio BebidaServiceImpl.buscar");
        try {
            return mapper.toResponse(obtener(id));
        } finally {
            log.info("Fin BebidaServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<BebidaResponseDTO> listar() {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio BebidaServiceImpl.listar");
        try {
            return repository.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(mapper::toResponse).toList();
        } finally {
            log.info("Fin BebidaServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio BebidaServiceImpl.eliminar");
        try {
            obtener(id).setActivo(false);
        } finally {
            log.info("Fin BebidaServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private Bebida obtener(Long id) {
        var e = repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Bebida no encontrado"));
        return e;
    }
}
