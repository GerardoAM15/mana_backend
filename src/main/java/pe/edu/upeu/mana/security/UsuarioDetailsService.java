package pe.edu.upeu.mana.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.repository.*;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UsuarioDetailsService.class);

    private final UsuarioRepository usuarios;

    private final EmpleadoRepository empleados;

    public UsuarioDetailsService(UsuarioRepository usuarios, EmpleadoRepository empleados) {
        this.usuarios = usuarios;
        this.empleados = empleados;
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio UsuarioDetailsService.loadUserByUsername");
        try {
            var u = usuarios.findByCorreoIgnoreCase(correo).orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
            var e = empleados.findById(u.getEmpleadoId()).orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
            return User.withUsername(u.getCorreo()).password(u.getClaveHash()).authorities(PermisosPorRol.permisos(u.getRol().name()).toArray(String[]::new)).disabled(!Boolean.TRUE.equals(u.getActivo()) || !Boolean.TRUE.equals(e.getActivo())).build();
        } finally {
            log.info("Fin UsuarioDetailsService.loadUserByUsername | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
