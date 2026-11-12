package pe.edu.upeu.mana.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.upeu.mana.entity.Usuario;

@Service
public class JwtService {

    public static final long EXPIRA_EN_SEGUNDOS = 3600;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtService.class);

    private final SecretKey clave;

    private final Clock clock;

    public JwtService(@Value("${app.jwt.secret}") String secret, Clock clock) {
        this.clave = cargarClave(secret);
        this.clock = clock;
    }

    private static SecretKey cargarClave(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Configure app.jwt.secret (JWT_SECRET) con una clave Base64 de al menos 32 bytes decodificados");
        }
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret);
        } catch (io.jsonwebtoken.io.DecodingException e) {
            throw new IllegalArgumentException("app.jwt.secret (JWT_SECRET) debe ser una clave Base64 valida, no un token JWT ni texto libre");
        }
        if (bytes.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret (JWT_SECRET) debe contener al menos 32 bytes decodificados");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generarToken(Usuario u) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio JwtService.generarToken");
        try {
            var ahora = clock.instant();
            return Jwts.builder().subject(u.getCorreo()).issuer("mana-api").claim("tv", u.getTokenVersion()).issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plusSeconds(EXPIRA_EN_SEGUNDOS))).signWith(clave).compact();
        } finally {
            log.debug("Fin JwtService.generarToken | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    public Claims validar(String token) {
        long inicioOperacion = System.nanoTime();
        log.debug("Inicio JwtService.validar");
        try {
            var c = Jwts.parser().verifyWith(clave).requireIssuer("mana-api").clock(() -> Date.from(clock.instant())).build().parseSignedClaims(token).getPayload();
            if (c.getSubject() == null || c.getSubject().isBlank() || c.getExpiration() == null || !(c.get("tv") instanceof Number))
                throw new JwtException("Token incompleto");
            return c;
        } finally {
            log.debug("Fin JwtService.validar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
