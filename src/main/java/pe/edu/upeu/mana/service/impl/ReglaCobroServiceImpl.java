package pe.edu.upeu.mana.service.impl;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.service.service.ReglaCobroService;
import pe.edu.upeu.mana.dto.menu.ComponenteReglaRequestDTO;
import pe.edu.upeu.mana.dto.menu.ReglaCobroRequestDTO;
import pe.edu.upeu.mana.dto.menu.ReglaCobroResponseDTO;

@Service
public class ReglaCobroServiceImpl implements ReglaCobroService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReglaCobroServiceImpl.class);

    private final pe.edu.upeu.mana.mapper.ReglaCobroMapper mapper;

    private final ReglaCobroRepository repository;

    private final ComponenteReglaRepository relaciones;

    private final ServicioRepository servicios;

    private final CategoriaPlatoRepository categorias;

    public ReglaCobroServiceImpl(pe.edu.upeu.mana.mapper.ReglaCobroMapper mapper, ReglaCobroRepository repository, ComponenteReglaRepository relaciones, ServicioRepository servicios, CategoriaPlatoRepository categorias) {
        this.mapper = mapper;
        this.repository = repository;
        this.relaciones = relaciones;
        this.servicios = servicios;
        this.categorias = categorias;
    }

    @Transactional
    public ReglaCobroResponseDTO crear(ReglaCobroRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReglaCobroServiceImpl.crear");
        try {
            validar(r, null);
            var e = new ReglaCobro();
            asignar(e, r);
            repository.saveAndFlush(e);
            vincular(e, r);
            return respuesta(e);
        } finally {
            log.debug("Fin ReglaCobroServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public ReglaCobroResponseDTO actualizar(Long id, ReglaCobroRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReglaCobroServiceImpl.actualizar");
        try {
            validar(r, id);
            var e = repository.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Registro no encontrado"));
            asignar(e, r);
            vincular(e, r);
            repository.flush();
            return respuesta(e);
        } finally {
            log.debug("Fin ReglaCobroServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public ReglaCobroResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReglaCobroServiceImpl.buscar");
        try {
            return respuesta(obtener(id));
        } finally {
            log.debug("Fin ReglaCobroServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<ReglaCobroResponseDTO> listar() {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReglaCobroServiceImpl.listar");
        try {
            return repository.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(this::respuesta).toList();
        } finally {
            log.debug("Fin ReglaCobroServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ReglaCobroServiceImpl.eliminar");
        try {
            obtener(id).setActivo(false);
        } finally {
            log.debug("Fin ReglaCobroServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private ReglaCobro obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Registro no encontrado"));
    }

    private void validar(ReglaCobroRequestDTO r, Long id) {
        // El servicio padre serializa altas y activaciones, incluso si aún no hay una regla.
        if (!servicios.buscarParaActualizar(r.getServicio()).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
            throw new ReglaNegocioException("Servicio inexistente o inactivo");
        if (Boolean.TRUE.equals(r.getActivo()) && repository.existsByServicioAndActivoTrueAndIdNot(r.getServicio(), id == null ? -1L : id))
            throw new ReglaNegocioException("Ya existe un menú activo para ese servicio; desactívelo primero");
        var codigos = new HashSet<String>();
        for (var c : r.getComponentes()) {
            if (!codigos.add(c.getCategoria()))
                throw new DatosInvalidosException("Categoría repetida en composición");
            if (!categorias.findById(c.getCategoria()).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
                throw new ReglaNegocioException("Categoría inexistente o inactiva");
        }
    }

    private void asignar(ReglaCobro e, ReglaCobroRequestDTO r) {
        e.setNombre(r.getNombre());
        e.setServicio(r.getServicio());
        e.setPrecioCombo(r.getPrecioCombo().setScale(2, java.math.RoundingMode.HALF_UP));
        e.setRecargoLlevar(r.getRecargoLlevar().setScale(2, java.math.RoundingMode.HALF_UP));
        e.setActivo(r.getActivo());
    }

    private void vincular(ReglaCobro e, ReglaCobroRequestDTO r) {
        relaciones.deleteAll(relaciones.findByReglaId(e.getId()));
        relaciones.flush();
        for (var c : r.getComponentes().stream().sorted(Comparator.comparing(ComponenteReglaRequestDTO::getCategoria)).toList()) {
            var link = new ComponenteRegla();
            link.setReglaId(e.getId());
            link.setCategoria(c.getCategoria());
            link.setCantidad(c.getCantidad());
            relaciones.save(link);
        }
    }

    private ReglaCobroResponseDTO respuesta(ReglaCobro e) {
        return mapper.toResponse(e, relaciones.findByReglaId(e.getId()));
    }
}
