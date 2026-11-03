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
import org.springframework.transaction.annotation.Transactional;

@Service
public class JwtService {

    public static final long EXPIRA_EN_SEGUNDOS = 3600;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtService.class);

    private final SecretKey clave;

    private final Clock clock;

    public JwtService(@Value("${app.jwt.secret}") String secret, Clock clock) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public String generarToken(Usuario u) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio JwtService.generarToken");
        try {
            var ahora = clock.instant();
            return Jwts.builder().subject(u.getCorreo()).issuer("mana-api").claim("tv", u.getTokenVersion()).issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plusSeconds(EXPIRA_EN_SEGUNDOS))).signWith(clave).compact();
        } finally {
            log.info("Fin JwtService.generarToken | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }

    @Transactional(readOnly = true)
    public Claims validar(String token) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio JwtService.validar");
        try {
            var c = Jwts.parser().verifyWith(clave).requireIssuer("mana-api").clock(() -> Date.from(clock.instant())).build().parseSignedClaims(token).getPayload();
            if (c.getSubject() == null || c.getSubject().isBlank() || c.getExpiration() == null || !(c.get("tv") instanceof Number))
                throw new JwtException("Token incompleto");
            return c;
        } finally {
            log.info("Fin JwtService.validar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
