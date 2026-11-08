package pe.edu.upeu.mana.service.impl;

import java.util.List;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.GastoDiario;
import pe.edu.upeu.mana.repository.GastoDiarioRepository;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.mapper.GastoDiarioMapper;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.gasto.GastoDiarioRequestDTO;
import pe.edu.upeu.mana.dto.gasto.GastoDiarioResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@Service
public class GastoDiarioServiceImpl implements GastoDiarioService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GastoDiarioServiceImpl.class);

    private static final java.util.Set<String> CAMPOS_ORDENABLES = java.util.Set.of("id", "fecha", "monto", "activo");

    private final GastoDiarioRepository repository;

    private final GastoDiarioMapper mapper;

    private final ContextoLocalService contexto;

    public GastoDiarioServiceImpl(GastoDiarioRepository repository, GastoDiarioMapper mapper, ContextoLocalService contexto) {
        this.repository = repository;
        this.mapper = mapper;
        this.contexto = contexto;
    }

    @Transactional
    public GastoDiarioResponseDTO crear(GastoDiarioRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio GastoDiarioServiceImpl.crear");
        try {
            var e = new GastoDiario();
            e.setLocalId(contexto.localId(null));
            mapper.actualizar(e, r);
            return mapper.toResponse(repository.saveAndFlush(e));
        } finally {
            log.debug("Fin GastoDiarioServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public GastoDiarioResponseDTO actualizar(Long id, GastoDiarioRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio GastoDiarioServiceImpl.actualizar");
        try {
            var e = obtener(id);
            mapper.actualizar(e, r);
            repository.flush();
            return mapper.toResponse(e);
        } finally {
            log.debug("Fin GastoDiarioServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public GastoDiarioResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio GastoDiarioServiceImpl.buscar");
        try {
            return mapper.toResponse(obtener(id));
        } finally {
            log.debug("Fin GastoDiarioServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<GastoDiarioResponseDTO> listar(Long localId, LocalDate desde, LocalDate hasta, int pagina, int tamanio, String ordenarPor, String direccion) {
        long inicio = System.nanoTime();
        log.debug("Inicio GastoDiarioServiceImpl.listar");
        try {
            var pageable = PaginacionUtil.construir(pagina, tamanio, ordenarPor, direccion, CAMPOS_ORDENABLES, "id");
            if (desde != null && hasta != null && desde.isAfter(hasta))
                throw new DatosInvalidosException("Rango de fechas inválido");
            return PaginaResponseDTO.de(repository.filtrar(contexto.localId(localId), desde, hasta, pageable).map(mapper::toResponse));
        } finally {
            log.debug("Fin GastoDiarioServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicio) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio GastoDiarioServiceImpl.eliminar");
        try {
            obtener(id).setActivo(false);
        } finally {
            log.debug("Fin GastoDiarioServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private GastoDiario obtener(Long id) {
        var e = repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("GastoDiario no encontrado"));
        contexto.validarLocal(e.getLocalId());
        return e;
    }
}
