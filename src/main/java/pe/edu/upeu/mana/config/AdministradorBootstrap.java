package pe.edu.upeu.mana.config;

import pe.edu.upeu.mana.enums.RolUsuario;
import java.util.Comparator;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;

@Component
public class AdministradorBootstrap implements ApplicationRunner {

    public AdministradorBootstrap(UsuarioRepository usuarios, EmpleadoRepository empleados, PersonaRepository personas, LocalRepository locales, PasswordEncoder encoder, @Value("${app.admin.email:}") String email, @Value("${app.admin.password:}") String password, @Value("${app.admin.dni:}") String dni) {
        this.usuarios = usuarios;
        this.empleados = empleados;
        this.personas = personas;
        this.locales = locales;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
        this.dni = dni;
    }

    private static final Logger log = LoggerFactory.getLogger(AdministradorBootstrap.class);

    private final UsuarioRepository usuarios;

    private final EmpleadoRepository empleados;

    private final PersonaRepository personas;

    private final LocalRepository locales;

    private final PasswordEncoder encoder;

    private final String email;

    private final String password;

    private final String dni;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existsByRol(RolUsuario.ADMINISTRADOR))
            return;
        if (email.isBlank() || password.isBlank() || dni.isBlank()) {
            log.warn("Primer administrador no creado: configure MANA_ADMIN_EMAIL, MANA_ADMIN_PASSWORD y MANA_ADMIN_DNI");
            return;
        }
        if (!dni.matches("[0-9]{8}") || password.length() < 8 || password.length() > 72 || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
            throw new IllegalStateException("Configuración del primer administrador inválida");
        var local = locales.findAll().stream().filter(l -> Boolean.TRUE.equals(l.getActivo())).min(Comparator.comparing(Local::getId)).orElse(null);
        if (local == null) {
            log.warn("Primer administrador no creado: no hay local activo");
            return;
        }
        if (personas.findAll().stream().anyMatch(p -> p.getDni().equals(dni)) || usuarios.findByCorreoIgnoreCase(email).isPresent()) {
            log.warn("Primer administrador no creado: DNI o correo en uso");
            return;
        }
        var e = new Empleado();
        e.setDni(dni);
        e.setNombre("Administrador");
        e.setApellido("MANÁ");
        e.setCargo("Administrador");
        e.setLocalId(local.getId());
        e.setActivo(true);
        empleados.saveAndFlush(e);
        var u = new Usuario();
        u.setEmpleadoId(e.getId());
        u.setCorreo(email.trim().toLowerCase(java.util.Locale.ROOT));
        u.setClaveHash(encoder.encode(password));
        u.setRol(RolUsuario.ADMINISTRADOR);
        u.setTokenVersion(0L);
        u.setActivo(true);
        usuarios.saveAndFlush(u);
        log.info("Primer administrador creado");
    }
}
