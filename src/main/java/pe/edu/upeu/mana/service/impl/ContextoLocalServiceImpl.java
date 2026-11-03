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

    @Transactional(readOnly = true)
    public Usuario usuario() {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ContextoLocalServiceImpl.usuario");
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated())
                throw new BadCredentialsException("Autenticación requerida");
            return usuarios.findByCorreoIgnoreCase(auth.getName()).orElseThrow(() -> new BadCredentialsException("Usuario inválido"));
        } finally {
            log.info("Fin ContextoLocalServiceImpl.usuario | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public Long localId(Long filtro) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ContextoLocalServiceImpl.localId");
        try {
            var u = usuario();
            var e = empleados.findById(u.getEmpleadoId()).orElseThrow(() -> new BadCredentialsException("Empleado inválido"));
            if (filtro == null)
                return e.getLocalId();
            if (!PermisosPorRol.esAdministradorODueno(u.getRol()) && !filtro.equals(e.getLocalId()))
                throw new AccessDeniedException("El local no corresponde al usuario");
            return filtro;
        } finally {
            log.info("Fin ContextoLocalServiceImpl.localId | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public void validarLocal(Long localId) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ContextoLocalServiceImpl.validarLocal");
        try {
            var u = usuario();
            if (!PermisosPorRol.esAdministradorODueno(u.getRol()) && !localId.equals(localId(null)))
                throw new AccessDeniedException("El local no corresponde al usuario");
        } finally {
            log.info("Fin ContextoLocalServiceImpl.validarLocal | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
