package pe.edu.upeu.mana.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.dto.catalogo.CatalogoResponseDTO;
import pe.edu.upeu.mana.dto.catalogo.CatalogosResponseDTO;
import pe.edu.upeu.mana.dto.catalogo.DiaAtencionResponseDTO;
import pe.edu.upeu.mana.service.service.CatalogoService;

@Service
public class CatalogoServiceImpl implements pe.edu.upeu.mana.service.service.CatalogoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CatalogoServiceImpl.class);

    private final ServicioRepository servicios;

    private final MetodoPagoRepository metodos;

    private final CategoriaPlatoRepository categorias;

    private final RolRepository roles;

    private final ServicioDiaAtencionRepository dias;

    public CatalogoServiceImpl(ServicioRepository servicios, MetodoPagoRepository metodos, CategoriaPlatoRepository categorias, RolRepository roles, ServicioDiaAtencionRepository dias) {
        this.servicios = servicios;
        this.metodos = metodos;
        this.categorias = categorias;
        this.roles = roles;
        this.dias = dias;
    }

    @Transactional(readOnly = true)
    public CatalogosResponseDTO listar() {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CatalogoServiceImpl.listar");
        try {
            return new CatalogosResponseDTO(servicios.findAll(org.springframework.data.domain.Sort.by("codigo")).stream().map(s -> new CatalogoResponseDTO(s.getCodigo(), s.getNombre(), s.getDescripcion(), s.getActivo())).toList(), metodos.findAll(org.springframework.data.domain.Sort.by("codigo")).stream().map(s -> new CatalogoResponseDTO(s.getCodigo(), s.getNombre(), s.getDescripcion(), s.getActivo())).toList(), categorias.findAll(org.springframework.data.domain.Sort.by("codigo")).stream().map(s -> new CatalogoResponseDTO(s.getCodigo(), s.getNombre(), s.getDescripcion(), s.getActivo())).toList(), roles.findAll(org.springframework.data.domain.Sort.by("codigo")).stream().map(s -> new CatalogoResponseDTO(s.getCodigo(), s.getNombre(), s.getDescripcion(), s.getActivo())).toList(), dias.findAll(org.springframework.data.domain.Sort.by("servicio", "diaSemana")).stream().map(s -> new DiaAtencionResponseDTO(s.getServicio(), s.getDiaSemana())).toList());
        } finally {
            log.info("Fin CatalogoServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
