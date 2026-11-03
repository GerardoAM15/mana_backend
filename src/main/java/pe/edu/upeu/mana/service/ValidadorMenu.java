package pe.edu.upeu.mana.service;

import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.dto.consumo.PlatoCantidadRequestDTO;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.*;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ValidadorMenu {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidadorMenu.class);

    private final ProgramacionMenuRepository menus;

    private final MenuPlatoRepository relaciones;

    private final PlatoRepository platos;

    private final ComponenteReglaRepository componentes;

    private final ReglaCobroRepository reglas;

    public ValidadorMenu(ProgramacionMenuRepository menus, MenuPlatoRepository relaciones, PlatoRepository platos, ComponenteReglaRepository componentes, ReglaCobroRepository reglas) {
        this.menus = menus;
        this.relaciones = relaciones;
        this.platos = platos;
        this.componentes = componentes;
        this.reglas = reglas;
    }

    @Transactional(readOnly = true)
    public ReglaCobro obtenerRegla(String servicio) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ValidadorMenu.obtenerRegla");
        try {
            var opciones = reglas.findByServicioAndActivoTrueOrderById(servicio);
            if (opciones.size() != 1)
                throw new ReglaNegocioException("Debe existir una única regla activa para el servicio del pensionista");
            return opciones.getFirst();
        } finally {
            log.info("Fin ValidadorMenu.obtenerRegla | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, Plato> validar(Long localId, LocalDate fecha, String servicio, ReglaCobro regla, List<PlatoCantidadRequestDTO> seleccion) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ValidadorMenu.validar");
        try {
            if (!regla.getServicio().equals(servicio) || !Boolean.TRUE.equals(regla.getActivo()))
                throw new ReglaNegocioException("La regla no corresponde al servicio");
            var menu = menus.findByLocalIdAndFechaAndServicio(localId, fecha, servicio).orElseThrow(() -> new ReglaNegocioException("No hay menú programado para hoy y ese servicio"));
            var permitidos = new HashSet<>(relaciones.findByMenuId(menu.getId()).stream().map(MenuPlato::getPlatoId).toList());
            var resultado = new TreeMap<Long, Plato>();
            var elegidos = new TreeMap<String, Long>();
            for (var linea : seleccion.stream().sorted(Comparator.comparing(PlatoCantidadRequestDTO::getPlatoId)).toList()) {
                if (resultado.containsKey(linea.getPlatoId()))
                    throw new DatosInvalidosException("No repita un plato en la selección");
                if (linea.getCantidad() == null || linea.getCantidad() < 1 || linea.getCantidad() > 9999999999L)
                    throw new DatosInvalidosException("Cantidad de plato inválida");
                if (!permitidos.contains(linea.getPlatoId()))
                    throw new ReglaNegocioException("El plato no está en la programación de hoy");
                var p = platos.findById(linea.getPlatoId()).orElseThrow(() -> new RecursoNoEncontradoException("Plato no encontrado"));
                if (!Boolean.TRUE.equals(p.getActivo()))
                    throw new ReglaNegocioException("El plato está inactivo");
                resultado.put(p.getId(), p);
                elegidos.merge(p.getCategoria(), linea.getCantidad(), Long::sum);
            }
            var requeridos = new TreeMap<String, Long>();
            componentes.findByReglaId(regla.getId()).forEach(c -> requeridos.put(c.getCategoria(), c.getCantidad()));
            if (requeridos.isEmpty() || !requeridos.equals(elegidos))
                throw new ReglaNegocioException("La selección debe cumplir exactamente la composición del menú");
            return resultado;
        } finally {
            log.info("Fin ValidadorMenu.validar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
