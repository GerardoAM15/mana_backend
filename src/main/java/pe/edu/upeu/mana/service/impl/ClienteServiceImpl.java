package pe.edu.upeu.mana.service.impl;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.ClienteMapper;
import pe.edu.upeu.mana.service.service.ClienteService;
import pe.edu.upeu.mana.dto.cliente.ClienteRequestDTO;
import pe.edu.upeu.mana.dto.cliente.ClienteResponseDTO;
import pe.edu.upeu.mana.dto.PaginaResponseDTO;
import pe.edu.upeu.mana.util.PaginacionUtil;

@Service
public class ClienteServiceImpl implements ClienteService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClienteServiceImpl.class);

    private static final java.util.Set<String> CAMPOS_ORDENABLES = java.util.Set.of("id", "dni", "nombre", "apellido", "activo");

    private final ClienteRepository repository;

    private final ClienteMapper mapper;

    private final PersonaRepository personas;

    public ClienteServiceImpl(ClienteRepository repository, ClienteMapper mapper, PersonaRepository personas) {
        this.repository = repository;
        this.mapper = mapper;
        this.personas = personas;
    }

    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ClienteServiceImpl.crear");
        try {
            var e = new Cliente();
            validar(e, r);
            mapper.actualizar(e, r);
            repository.saveAndFlush(e);
            return mapper.toResponse(e);
        } finally {
            log.info("Fin ClienteServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ClienteServiceImpl.actualizar");
        try {
            var e = obtener(id);
            validar(e, r);
            mapper.actualizar(e, r);
            repository.flush();
            return mapper.toResponse(e);
        } finally {
            log.info("Fin ClienteServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ClienteServiceImpl.buscar");
        try {
            return mapper.toResponse(obtener(id));
        } finally {
            log.info("Fin ClienteServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ClienteResponseDTO> listar(String q, int pagina, int tamanio, String ordenarPor, String direccion) {
        long inicio = System.nanoTime();
        log.info("Inicio ClienteServiceImpl.listar");
        try {
            var pageable = PaginacionUtil.construir(pagina, tamanio, ordenarPor, direccion, CAMPOS_ORDENABLES, "id");
            if (q == null || q.isBlank())
                return PaginaResponseDTO.de(repository.findAll(pageable).map(mapper::toResponse));
            String busqueda = q.trim();
            String texto = "%" + busqueda.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            return PaginaResponseDTO.de(repository.filtrar(busqueda, texto, pageable).map(mapper::toResponse));
        } finally {
            log.info("Fin ClienteServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicio) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ClienteServiceImpl.eliminar");
        try {
            obtener(id).setActivo(false);
        } finally {
            log.info("Fin ClienteServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private Cliente obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
    }

    private void validar(Cliente e, ClienteRequestDTO r) {
        if (personas.existsByDniAndIdNot(r.getDni(), e.getId() == null ? -1L : e.getId()))
            throw new ReglaNegocioException("El DNI ya está registrado");
    }
}
