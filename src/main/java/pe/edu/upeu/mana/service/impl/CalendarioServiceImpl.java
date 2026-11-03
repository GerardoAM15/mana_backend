package pe.edu.upeu.mana.service.impl;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.DiaNoAtencion;
import pe.edu.upeu.mana.repository.DiaNoAtencionRepository;
import pe.edu.upeu.mana.mapper.DiaNoAtencionMapper;
import pe.edu.upeu.mana.service.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.service.service.*;
import pe.edu.upeu.mana.dto.calendario.DiaNoAtencionRequestDTO;
import pe.edu.upeu.mana.dto.calendario.DiaNoAtencionResponseDTO;

@Service
public class CalendarioServiceImpl implements CalendarioService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CalendarioServiceImpl.class);

    private final DiaNoAtencionRepository dias;

    private final ContextoLocalService contexto;

    private final DiaNoAtencionMapper mapper;

    public CalendarioServiceImpl(DiaNoAtencionRepository dias, ContextoLocalService contexto, DiaNoAtencionMapper mapper) {
        this.dias = dias;
        this.contexto = contexto;
        this.mapper = mapper;
    }

    @Transactional
    public DiaNoAtencionResponseDTO crear(DiaNoAtencionRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CalendarioServiceImpl.crear");
        try {
            var e = new DiaNoAtencion();
            e.setLocalId(contexto.localId(null));
            e.setFecha(r.getFecha());
            e.setMotivo(r.getMotivo());
            return mapper.toResponse(dias.saveAndFlush(e));
        } finally {
            log.info("Fin CalendarioServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public DiaNoAtencionResponseDTO actualizar(Long id, DiaNoAtencionRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CalendarioServiceImpl.actualizar");
        try {
            var e = obtener(id);
            e.setFecha(r.getFecha());
            e.setMotivo(r.getMotivo());
            dias.flush();
            return mapper.toResponse(e);
        } finally {
            log.info("Fin CalendarioServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<DiaNoAtencionResponseDTO> listar(Long localId, LocalDate desde, LocalDate hasta) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CalendarioServiceImpl.listar");
        try {
            if (desde != null && hasta != null && desde.isAfter(hasta))
                throw new DatosInvalidosException("El inicio no puede ser posterior al fin");
            return dias.filtrar(contexto.localId(localId), desde, hasta).stream().map(mapper::toResponse).toList();
        } finally {
            log.info("Fin CalendarioServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio CalendarioServiceImpl.eliminar");
        try {
            dias.delete(obtener(id));
        } finally {
            log.info("Fin CalendarioServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private DiaNoAtencion obtener(Long id) {
        var e = dias.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Día no encontrado"));
        contexto.validarLocal(e.getLocalId());
        return e;
    }
}
