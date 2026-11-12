package pe.edu.upeu.mana.service.impl;

import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.mapper.MenuMapper;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.menu.ClonarMenuRequestDTO;
import pe.edu.upeu.mana.dto.menu.CopiarCenaRequestDTO;
import pe.edu.upeu.mana.dto.menu.MenuRequestDTO;
import pe.edu.upeu.mana.dto.menu.MenuResponseDTO;

@Service
public class MenuServiceImpl implements MenuService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MenuServiceImpl.class);

    private final ProgramacionMenuRepository menus;

    private final MenuPlatoRepository relaciones;

    private final PlatoRepository platos;

    private final ServicioRepository servicios;

    private final ContextoLocalService contexto;

    private final MenuMapper mapper;

    private final ValidadorAtencion atencion;

    public MenuServiceImpl(ProgramacionMenuRepository menus, MenuPlatoRepository relaciones, PlatoRepository platos, ServicioRepository servicios, ContextoLocalService contexto, MenuMapper mapper, ValidadorAtencion atencion) {
        this.menus = menus;
        this.relaciones = relaciones;
        this.platos = platos;
        this.servicios = servicios;
        this.contexto = contexto;
        this.mapper = mapper;
        this.atencion = atencion;
    }

    @Transactional
    public MenuResponseDTO guardar(MenuRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio MenuServiceImpl.guardar");
        try {
            return respuesta(guardar(contexto.localId(null), r.getFecha(), r.getServicio(), r.getPlatoIds()));
        } finally {
            log.debug("Fin MenuServiceImpl.guardar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private ProgramacionMenu guardar(Long localId, LocalDate fecha, String servicio, Set<Long> ids) {
        if (!servicios.findById(servicio).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false))
            throw new ReglaNegocioException("Servicio inexistente o inactivo");
        atencion.validar(localId, fecha, servicio);
        var seleccion = platos.findAllById(ids);
        if (seleccion.size() != ids.size() || seleccion.stream().anyMatch(p -> !Boolean.TRUE.equals(p.getActivo())))
            throw new ReglaNegocioException("Hay platos inexistentes o inactivos");
        var e = menus.buscarFechaParaActualizar(localId, fecha, servicio).orElseGet(ProgramacionMenu::new);
        e.setLocalId(localId);
        e.setFecha(fecha);
        e.setServicio(servicio);
        menus.saveAndFlush(e);
        relaciones.deleteAll(relaciones.findByMenuId(e.getId()));
        relaciones.flush();
        for (Long id : ids.stream().sorted().toList()) {
            var p = new MenuPlato();
            p.setMenuId(e.getId());
            p.setPlatoId(id);
            relaciones.save(p);
        }
        relaciones.flush();
        return e;
    }

    @Transactional(readOnly = true)
    public List<MenuResponseDTO> listar(Long localId, LocalDate desde, LocalDate hasta, String servicio) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio MenuServiceImpl.listar");
        try {
            if (desde != null && hasta != null && desde.isAfter(hasta))
                throw new DatosInvalidosException("Rango de fechas inválido");
            return menus.filtrar(contexto.localId(localId), desde, hasta, servicio).stream().map(this::respuesta).toList();
        } finally {
            log.debug("Fin MenuServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public List<MenuResponseDTO> clonar(ClonarMenuRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio MenuServiceImpl.clonar");
        try {
            if (r.getFechaOrigen().equals(r.getFechaDestino()))
                throw new DatosInvalidosException("Las fechas origen y destino deben ser distintas");
            Long local = contexto.localId(null);
            var origen = menus.filtrar(local, r.getFechaOrigen(), r.getFechaOrigen(), null).stream().filter(m -> r.getServicios() == null || r.getServicios().isEmpty() || r.getServicios().contains(m.getServicio())).toList();
            if (origen.isEmpty())
                throw new RecursoNoEncontradoException("No hay menús en la fecha origen");
            var resultado = new ArrayList<MenuResponseDTO>();
            for (var e : origen) {
                if (!atencion.seAtiende(local, r.getFechaDestino(), e.getServicio())) continue;
                var ids = new HashSet<>(relaciones.findByMenuId(e.getId()).stream().map(MenuPlato::getPlatoId).toList());
                resultado.add(respuesta(guardar(local, r.getFechaDestino(), e.getServicio(), ids)));
            }
            return resultado;
        } finally {
            log.debug("Fin MenuServiceImpl.clonar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public MenuResponseDTO copiarACena(CopiarCenaRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio MenuServiceImpl.copiarACena");
        try {
            Long local = contexto.localId(null);
            atencion.validar(local, r.getFecha(), "C");
            var almuerzo = menus.findByLocalIdAndFechaAndServicio(local, r.getFecha(), "A").orElseThrow(() -> new RecursoNoEncontradoException("No hay menú de almuerzo"));
            var ids = new HashSet<Long>();
            for (var p : obtenerPlatos(almuerzo)) if (p.getCategoria().equals("SEGUNDO"))
                ids.add(p.getId());
            if (ids.isEmpty())
                throw new ReglaNegocioException("El almuerzo no tiene segundos");
            menus.findByLocalIdAndFechaAndServicio(local, r.getFecha(), "C").ifPresent(c -> relaciones.findByMenuId(c.getId()).forEach(p -> ids.add(p.getPlatoId())));
            return respuesta(guardar(local, r.getFecha(), "C", ids));
        } finally {
            log.debug("Fin MenuServiceImpl.copiarACena | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private List<Plato> obtenerPlatos(ProgramacionMenu e) {
        var ids = relaciones.findByMenuId(e.getId()).stream().map(MenuPlato::getPlatoId).sorted().toList();
        return platos.findAllById(ids).stream().sorted(Comparator.comparing(Plato::getId)).toList();
    }

    private MenuResponseDTO respuesta(ProgramacionMenu e) {
        return mapper.toResponse(e, obtenerPlatos(e));
    }
}
