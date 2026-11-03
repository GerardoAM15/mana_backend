package pe.edu.upeu.mana.service.impl;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.service.service.PlanService;
import pe.edu.upeu.mana.dto.plan.PlanRequestDTO;
import pe.edu.upeu.mana.dto.plan.PlanResponseDTO;

@Service
public class PlanServiceImpl implements PlanService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlanServiceImpl.class);

    private final pe.edu.upeu.mana.mapper.PlanMapper mapper;

    private final TipoPlanRepository repository;

    private final PlanServicioRepository relaciones;

    private final ServicioRepository servicios;

    public PlanServiceImpl(pe.edu.upeu.mana.mapper.PlanMapper mapper, TipoPlanRepository repository, PlanServicioRepository relaciones, ServicioRepository servicios) {
        this.mapper = mapper;
        this.repository = repository;
        this.relaciones = relaciones;
        this.servicios = servicios;
    }

    @Transactional
    public PlanResponseDTO crear(PlanRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio PlanServiceImpl.crear");
        try {
            validar(r);
            var e = new TipoPlan();
            asignar(e, r);
            repository.saveAndFlush(e);
            vincular(e, r);
            return respuesta(e);
        } finally {
            log.info("Fin PlanServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public PlanResponseDTO actualizar(Long id, PlanRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio PlanServiceImpl.actualizar");
        try {
            validar(r);
            var e = repository.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Registro no encontrado"));
            asignar(e, r);
            vincular(e, r);
            repository.flush();
            return respuesta(e);
        } finally {
            log.info("Fin PlanServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public PlanResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio PlanServiceImpl.buscar");
        try {
            return respuesta(obtener(id));
        } finally {
            log.info("Fin PlanServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<PlanResponseDTO> listar() {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio PlanServiceImpl.listar");
        try {
            return repository.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(this::respuesta).toList();
        } finally {
            log.info("Fin PlanServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio PlanServiceImpl.eliminar");
        try {
            obtener(id).setActivo(false);
        } finally {
            log.info("Fin PlanServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private TipoPlan obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Registro no encontrado"));
    }

    private void validar(PlanRequestDTO r) {
        for (var codigo : r.getServicios()) if (!servicios.findById(codigo).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
            throw new ReglaNegocioException("Servicio inexistente o inactivo");
    }

    private void asignar(TipoPlan e, PlanRequestDTO r) {
        e.setNombre(r.getNombre());
        e.setPeriodo(r.getPeriodo());
        e.setActivo(r.getActivo());
    }

    private void vincular(TipoPlan e, PlanRequestDTO r) {
        relaciones.deleteAll(relaciones.findByPlanId(e.getId()));
        relaciones.flush();
        for (var codigo : r.getServicios().stream().sorted().toList()) {
            var link = new PlanServicio();
            link.setPlanId(e.getId());
            link.setServicio(codigo);
            relaciones.save(link);
        }
    }

    private PlanResponseDTO respuesta(TipoPlan e) {
        return mapper.toResponse(e, relaciones.findByPlanId(e.getId()));
    }
}
