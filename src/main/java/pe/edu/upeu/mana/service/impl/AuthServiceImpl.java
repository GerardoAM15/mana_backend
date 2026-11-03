package pe.edu.upeu.mana.service.impl;

import java.time.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.slf4j.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.security.JwtService;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.exception.DatosInvalidosException;
import pe.edu.upeu.mana.mapper.UsuarioMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.service.AuthService;
import pe.edu.upeu.mana.dto.auth.LoginRequestDTO;
import pe.edu.upeu.mana.dto.auth.LoginResponseDTO;
import pe.edu.upeu.mana.dto.auth.RecuperarClaveRequestDTO;
import pe.edu.upeu.mana.dto.auth.RestablecerClaveRequestDTO;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UsuarioRepository usuarios;

    private final EmpleadoRepository empleados;

    private final LocalRepository locales;

    private final TokenRestablecimientoRepository tokens;

    private final PasswordEncoder encoder;

    private final JwtService jwt;

    private final UsuarioMapper mapper;

    private final Clock clock;

    private final ObjectProvider<JavaMailSender> correo;

    private final Environment environment;

    private final String frontendUrl;

    private final SecureRandom random = new SecureRandom();

    public AuthServiceImpl(UsuarioRepository usuarios, EmpleadoRepository empleados, LocalRepository locales, TokenRestablecimientoRepository tokens, PasswordEncoder encoder, JwtService jwt, UsuarioMapper mapper, Clock clock, ObjectProvider<JavaMailSender> correo, Environment environment, @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.usuarios = usuarios;
        this.empleados = empleados;
        this.locales = locales;
        this.tokens = tokens;
        this.encoder = encoder;
        this.jwt = jwt;
        this.mapper = mapper;
        this.clock = clock;
        this.correo = correo;
        this.environment = environment;
        this.frontendUrl = frontendUrl;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio AuthServiceImpl.login");
        try {
            var u = usuarios.findByCorreoIgnoreCase(r.getCorreo().trim()).orElse(null);
            // El hash de reserva evita omitir BCrypt cuando el correo no existe.
            String hash = u == null ? "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy" : u.getClaveHash();
            boolean valida = encoder.matches(r.getClave(), hash);
            if (u == null || !valida || !Boolean.TRUE.equals(u.getActivo()))
                throw credenciales();
            var e = empleados.findById(u.getEmpleadoId()).orElseThrow(this::credenciales);
            if (!Boolean.TRUE.equals(e.getActivo()))
                throw credenciales();
            var l = locales.findById(e.getLocalId()).orElseThrow(this::credenciales);
            if (!Boolean.TRUE.equals(l.getActivo()))
                throw credenciales();
            return mapper.toResponse(u, e, l, jwt.generarToken(u), JwtService.EXPIRA_EN_SEGUNDOS);
        } finally {
            log.info("Fin AuthServiceImpl.login | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO buscarActual(String correo) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio AuthServiceImpl.buscarActual");
        try {
            var u = usuarios.findByCorreoIgnoreCase(correo).orElseThrow(this::credenciales);
            var e = empleados.findById(u.getEmpleadoId()).orElseThrow(this::credenciales);
            return mapper.toResponse(u, e, locales.findById(e.getLocalId()).orElseThrow(this::credenciales), null, null);
        } finally {
            log.info("Fin AuthServiceImpl.buscarActual | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void recuperar(RecuperarClaveRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio AuthServiceImpl.recuperar");
        try {
            var u = usuarios.findByCorreoIgnoreCase(r.getCorreo().trim()).orElse(null);
            if (u == null || !Boolean.TRUE.equals(u.getActivo()))
                return;
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            var t = new TokenRestablecimiento();
            t.setUsuarioId(u.getId());
            t.setTokenHash(hash(token));
            t.setExpiraEn(OffsetDateTime.now(clock).plusMinutes(5));
            t.setUsado(false);
            tokens.save(t);
            String enlace = frontendUrl + "/restablecer?token=" + token;
            var sender = correo.getIfAvailable();
            if (sender == null && environment.matchesProfiles("dev")) {
                // Excepción explícita para desarrollo solicitada: enlace de recuperación sin SMTP.
                log.info("Recuperación de desarrollo | enlace={}", enlace);
                return;
            }
            if (sender == null) {
                log.warn("SMTP no configurado para recuperación");
                return;
            }
            try {
                var mensaje = new SimpleMailMessage();
                mensaje.setTo(u.getCorreo());
                mensaje.setSubject("MANÁ: restablecer contraseña");
                mensaje.setText("El enlace vence en 5 minutos: " + enlace);
                sender.send(mensaje);
            } catch (org.springframework.mail.MailException ex) {
                log.warn("No se pudo enviar el correo de recuperación");
            }
        } finally {
            log.info("Fin AuthServiceImpl.recuperar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional
    public void restablecer(RestablecerClaveRequestDTO r) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio AuthServiceImpl.restablecer");
        try {
            var t = tokens.buscarPorHashParaActualizar(hash(r.getToken())).orElseThrow(() -> new DatosInvalidosException("Token inválido o vencido"));
            if (Boolean.TRUE.equals(t.getUsado()) || !t.getExpiraEn().isAfter(OffsetDateTime.now(clock)))
                throw new DatosInvalidosException("Token inválido o vencido");
            var u = usuarios.buscarParaActualizar(t.getUsuarioId()).orElseThrow(this::credenciales);
            if (!Boolean.TRUE.equals(u.getActivo()))
                throw new DatosInvalidosException("Token inválido o vencido");
            u.setClaveHash(encoder.encode(r.getNuevaClave()));
            u.setTokenVersion(u.getTokenVersion() + 1);
            t.setUsado(true);
        } finally {
            log.info("Fin AuthServiceImpl.restablecer | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    private BadCredentialsException credenciales() {
        return new BadCredentialsException("Credenciales inválidas");
    }

    private String hash(String texto) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
