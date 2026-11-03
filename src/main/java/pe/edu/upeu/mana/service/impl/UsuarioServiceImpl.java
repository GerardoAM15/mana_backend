package pe.edu.upeu.mana.service.impl;

import pe.edu.upeu.mana.enums.RolUsuario;
import java.util.*;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.mapper.UsuarioGestionMapper;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.service.service.UsuarioService;
import pe.edu.upeu.mana.dto.usuario.RolRequestDTO;
import pe.edu.upeu.mana.dto.usuario.UsuarioRequestDTO;
import pe.edu.upeu.mana.dto.usuario.UsuarioResponseDTO;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarios;

    private final EmpleadoRepository empleados;

    private final PersonaRepository personas;

    private final LocalRepository locales;

    private final RolRepository roles;

    private final PasswordEncoder encoder;

    private final UsuarioGestionMapper mapper;

    public UsuarioServiceImpl(UsuarioRepository usuarios, EmpleadoRepository empleados, PersonaRepository personas, LocalRepository locales, RolRepository roles, PasswordEncoder encoder, UsuarioGestionMapper mapper) {
        this.usuarios = usuarios;
        this.empleados = empleados;
        this.personas = personas;
        this.locales = locales;
        this.roles = roles;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio UsuarioServiceImpl.crear");
        try {
            if (r.getClave() == null || r.getClave().isBlank())
                throw new DatosInvalidosException("La clave es obligatoria al crear un usuario");
            var u = new Usuario();
            var e = new Empleado();
            asignar(u, e, r);
            empleados.saveAndFlush(e);
            u.setEmpleadoId(e.getId());
            u.setTokenVersion(0L);
            usuarios.saveAndFlush(u);
            return respuesta(u);
        } finally {
            log.info("Fin UsuarioServiceImpl.crear | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio UsuarioServiceImpl.actualizar");
        try {
            var u = usuarios.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            var e = empleados.buscarParaActualizar(u.getEmpleadoId()).orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado"));
            if (u.getRol() != r.getRol() || !u.getActivo().equals(r.getActivo()) || !u.getCorreo().equalsIgnoreCase(r.getCorreo().trim()) || !e.getLocalId().equals(r.getLocalId()) || r.getClave() != null)
                u.setTokenVersion(u.getTokenVersion() + 1);
            asignar(u, e, r);
            usuarios.flush();
            return respuesta(u);
        } finally {
            log.info("Fin UsuarioServiceImpl.actualizar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private void asignar(Usuario u, Empleado e, UsuarioRequestDTO r) {
        if (personas.existsByDniAndIdNot(r.getDni(), e.getId() == null ? -1L : e.getId()))
            throw new ReglaNegocioException("El DNI ya está registrado");
        var otro = usuarios.findByCorreoIgnoreCase(r.getCorreo().trim()).orElse(null);
        if (otro != null && !otro.getId().equals(u.getId()))
            throw new ReglaNegocioException("El correo ya está registrado");
        validarRol(r.getRol());
        if (!locales.findById(r.getLocalId()).map(l -> Boolean.TRUE.equals(l.getActivo())).orElse(false))
            throw new ReglaNegocioException("Local inexistente o inactivo");
        if (r.getClave() != null) {
            if (r.getClave().getBytes(StandardCharsets.UTF_8).length > 72)
                throw new DatosInvalidosException("La clave admite hasta 72 bytes UTF-8");
            u.setClaveHash(encoder.encode(r.getClave()));
        }
        e.setDni(r.getDni());
        e.setNombre(r.getNombre());
        e.setApellido(r.getApellido());
        e.setTelefono(r.getTelefono());
        e.setCargo(r.getCargo());
        e.setLocalId(r.getLocalId());
        e.setActivo(r.getActivo());
        u.setCorreo(r.getCorreo().trim().toLowerCase(Locale.ROOT));
        u.setRol(r.getRol());
        u.setActivo(r.getActivo());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio UsuarioServiceImpl.buscar");
        try {
            return respuesta(obtener(id));
        } finally {
            log.info("Fin UsuarioServiceImpl.buscar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio UsuarioServiceImpl.listar");
        try {
            return usuarios.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(this::respuesta).toList();
        } finally {
            log.info("Fin UsuarioServiceImpl.listar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public UsuarioResponseDTO cambiarRol(Long id, RolRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio UsuarioServiceImpl.cambiarRol");
        try {
            validarRol(r.getRol());
            var u = usuarios.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            if (u.getRol() != r.getRol()) {
                u.setRol(r.getRol());
                u.setTokenVersion(u.getTokenVersion() + 1);
            }
            usuarios.flush();
            return respuesta(u);
        } finally {
            log.info("Fin UsuarioServiceImpl.cambiarRol | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio UsuarioServiceImpl.eliminar");
        try {
            var u = usuarios.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            u.setActivo(false);
            u.setTokenVersion(u.getTokenVersion() + 1);
        } finally {
            log.info("Fin UsuarioServiceImpl.eliminar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private void validarRol(RolUsuario rol) {
        if (!roles.findById(rol.name()).map(r -> Boolean.TRUE.equals(r.getActivo())).orElse(false))
            throw new ReglaNegocioException("Rol inexistente o inactivo");
    }

    private Usuario obtener(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private UsuarioResponseDTO respuesta(Usuario u) {
        var e = empleados.findById(u.getEmpleadoId()).orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado"));
        return mapper.toResponse(u, e, locales.findById(e.getLocalId()).orElseThrow(() -> new RecursoNoEncontradoException("Local no encontrado")));
    }
}
