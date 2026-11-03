package pe.edu.upeu.mana.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.mapper.PrecioSueltoMapper;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.service.service.PrecioSueltoService;
import pe.edu.upeu.mana.dto.menu.PrecioSueltoRequestDTO;
import pe.edu.upeu.mana.dto.menu.PrecioSueltoResponseDTO;

@Service
public class PrecioSueltoServiceImpl implements PrecioSueltoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PrecioSueltoServiceImpl.class);

    private final PrecioSueltoRepository precios;

    private final ServicioRepository servicios;

    private final CategoriaPlatoRepository categorias;

    private final PrecioSueltoMapper mapper;

    public PrecioSueltoServiceImpl(PrecioSueltoRepository precios, ServicioRepository servicios, CategoriaPlatoRepository categorias, PrecioSueltoMapper mapper) {
        this.precios = precios;
        this.servicios = servicios;
        this.categorias = categorias;
        this.mapper = mapper;
    }

    @Transactional
    public PrecioSueltoResponseDTO crear(PrecioSueltoRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio PrecioSueltoServiceImpl.crear");
        try {
            validar(r);
            if (precios.existsById(new PrecioSueltoId(r.getServicio(), r.getCategoria())))
                throw new ReglaNegocioException("Ya existe el precio de esa categoría y servicio");
            var e = new PrecioSuelto();
            mapper.actualizar(e, r);
            precios.saveAndFlush(e);
            return mapper.toResponse(e);
        } finally {
            log.info("Fin PrecioSueltoServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public PrecioSueltoResponseDTO actualizar(String servicio, String categoria, PrecioSueltoRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio PrecioSueltoServiceImpl.actualizar");
        try {
            if (!servicio.equals(r.getServicio()) || !categoria.equals(r.getCategoria()))
                throw new DatosInvalidosException("La clave del precio no se puede cambiar");
            validar(r);
            var e = obtener(servicio, categoria);
            mapper.actualizar(e, r);
            precios.flush();
            return mapper.toResponse(e);
        } finally {
            log.info("Fin PrecioSueltoServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<PrecioSueltoResponseDTO> listar() {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio PrecioSueltoServiceImpl.listar");
        try {
            return precios.findAll(org.springframework.data.domain.Sort.by("servicio", "categoria")).stream().map(mapper::toResponse).toList();
        } finally {
            log.info("Fin PrecioSueltoServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(String servicio, String categoria) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio PrecioSueltoServiceImpl.eliminar");
        try {
            obtener(servicio, categoria).setActivo(false);
        } finally {
            log.info("Fin PrecioSueltoServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private PrecioSuelto obtener(String s, String c) {
        return precios.findById(new PrecioSueltoId(s, c)).orElseThrow(() -> new RecursoNoEncontradoException("Precio no encontrado"));
    }

    private void validar(PrecioSueltoRequestDTO r) {
        if (!servicios.findById(r.getServicio()).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
            throw new ReglaNegocioException("Servicio inexistente o inactivo");
        if (!categorias.findById(r.getCategoria()).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
            throw new ReglaNegocioException("Categoría inexistente o inactiva");
    }
}
