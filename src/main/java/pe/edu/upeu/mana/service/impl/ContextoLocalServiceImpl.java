package pe.edu.upeu.mana.service.impl;

import pe.edu.upeu.mana.security.PermisosPorRol;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.service.ContextoLocalService;

@Service
public class ContextoLocalServiceImpl implements ContextoLocalService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContextoLocalServiceImpl.class);

    private final UsuarioRepository usuarios;

    private final EmpleadoRepository empleados;

    public ContextoLocalServiceImpl(UsuarioRepository usuarios, EmpleadoRepository empleados) {
        this.usuarios = usuarios;
        this.empleados = empleados;
    }

    private jakarta.servlet.http.HttpServletRequest request() {
        var atributos = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        return atributos instanceof org.springframework.web.context.request.ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }

    @Transactional(readOnly = true)
    public Usuario usuario() {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContextoLocalServiceImpl.usuario");
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated())
                throw new BadCredentialsException("Autenticación requerida");
            var request = request();
            String clave = "mana.usuario:" + auth.getName();
            if (request != null && request.getAttribute(clave) instanceof Usuario cached) return cached;
            var usuario = usuarios.findByCorreoIgnoreCase(auth.getName()).orElseThrow(() -> new BadCredentialsException("Usuario inválido"));
            if (request != null) request.setAttribute(clave, usuario);
            return usuario;
        } finally {
            log.debug("Fin ContextoLocalServiceImpl.usuario | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public Long localId(Long filtro) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContextoLocalServiceImpl.localId");
        try {
            var u = usuario();
            var request = request();
            String clave = "mana.empleado:" + u.getEmpleadoId();
            var e = request != null && request.getAttribute(clave) instanceof Empleado cached ? cached
                    : empleados.findById(u.getEmpleadoId()).orElseThrow(() -> new BadCredentialsException("Empleado inválido"));
            if (request != null) request.setAttribute(clave, e);
            if (filtro == null)
                return e.getLocalId();
            if (!PermisosPorRol.esAdministradorODueno(u.getRol()) && !filtro.equals(e.getLocalId()))
                throw new AccessDeniedException("El local no corresponde al usuario");
            return filtro;
        } finally {
            log.debug("Fin ContextoLocalServiceImpl.localId | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public void validarLocal(Long localId) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio ContextoLocalServiceImpl.validarLocal");
        try {
            var u = usuario();
            if (!PermisosPorRol.esAdministradorODueno(u.getRol()) && !localId.equals(localId(null)))
                throw new AccessDeniedException("El local no corresponde al usuario");
        } finally {
            log.debug("Fin ContextoLocalServiceImpl.validarLocal | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
