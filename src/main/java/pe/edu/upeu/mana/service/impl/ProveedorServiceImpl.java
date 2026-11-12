package pe.edu.upeu.mana.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.mapper.ProveedorMapper;
import pe.edu.upeu.mana.service.service.ProveedorService;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.dto.proveedor.ProveedorRequestDTO;
import pe.edu.upeu.mana.dto.proveedor.ProveedorResponseDTO;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProveedorServiceImpl.class);

    private final ProveedorRepository proveedores;

    private final ProveedorBebidaRepository relaciones;

    private final BebidaRepository bebidas;

    private final ProveedorMapper mapper;

    public ProveedorServiceImpl(ProveedorRepository proveedores, ProveedorBebidaRepository relaciones, BebidaRepository bebidas, ProveedorMapper mapper) {
        this.proveedores = proveedores;
        this.relaciones = relaciones;
        this.bebidas = bebidas;
        this.mapper = mapper;
    }

    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ProveedorServiceImpl.crear");
        try {
            var p = new Proveedor();
            asignar(p, r);
            proveedores.saveAndFlush(p);
            vincular(p, r);
            return respuesta(p);
        } finally {
            log.debug("Fin ProveedorServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ProveedorServiceImpl.actualizar");
        try {
            var p = proveedores.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado"));
            asignar(p, r);
            vincular(p, r);
            proveedores.flush();
            return respuesta(p);
        } finally {
            log.debug("Fin ProveedorServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private void asignar(Proveedor p, ProveedorRequestDTO r) {
        p.setRazonSocial(r.getRazonSocial());
        p.setTelefono(r.getTelefono());
        p.setActivo(r.getActivo());
    }

    private void vincular(Proveedor p, ProveedorRequestDTO r) {
        for (Long id : r.getBebidaIds().stream().sorted().toList()) if (!bebidas.existsById(id))
            throw new RecursoNoEncontradoException("Bebida no encontrada");
        relaciones.deleteAll(relaciones.findByProveedorId(p.getId()));
        relaciones.flush();
        for (Long id : r.getBebidaIds().stream().sorted().toList()) {
            var b = new ProveedorBebida();
            b.setProveedorId(p.getId());
            b.setBebidaId(id);
            relaciones.save(b);
        }
    }

    @Transactional(readOnly = true)
    public ProveedorResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ProveedorServiceImpl.buscar");
        try {
            return respuesta(obtener(id));
        } finally {
            log.debug("Fin ProveedorServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listar() {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ProveedorServiceImpl.listar");
        try {
            return proveedores.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(this::respuesta).toList();
        } finally {
            log.debug("Fin ProveedorServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ProveedorServiceImpl.eliminar");
        try {
            obtener(id).setActivo(false);
        } finally {
            log.debug("Fin ProveedorServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private Proveedor obtener(Long id) {
        return proveedores.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado"));
    }

    private ProveedorResponseDTO respuesta(Proveedor p) {
        return mapper.toResponse(p, relaciones.findByProveedorId(p.getId()));
    }
}
