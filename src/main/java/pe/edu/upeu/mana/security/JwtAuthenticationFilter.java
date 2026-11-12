package pe.edu.upeu.mana.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upeu.mana.security.*;
import pe.edu.upeu.mana.repository.UsuarioRepository;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public JwtAuthenticationFilter(JwtService jwt, UsuarioDetailsService detalles, UsuarioRepository usuarios, RestAuthenticationEntryPoint entry) {
        this.jwt = jwt;
        this.detalles = detalles;
        this.usuarios = usuarios;
        this.entry = entry;
    }

    private final JwtService jwt;

    private final UsuarioDetailsService detalles;

    private final UsuarioRepository usuarios;

    private final RestAuthenticationEntryPoint entry;

    protected boolean shouldNotFilter(HttpServletRequest req) {
        return req.getMethod().equals("OPTIONS") || req.getServletPath().matches("/api/v1/auth/(login|recuperar|restablecer)");
    }

    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        var header = req.getHeader("Authorization");
        if (header == null) {
            chain.doFilter(req, res);
            return;
        }
        try {
            if (!header.startsWith("Bearer "))
                throw new BadCredentialsException("Token inválido");
            var claims = jwt.validar(header.substring(7));
            var usuario = usuarios.findByCorreoIgnoreCase(claims.getSubject()).orElseThrow(() -> new BadCredentialsException("Token inválido"));
            if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getTokenVersion() != ((Number) claims.get("tv")).longValue())
                throw new BadCredentialsException("Token inválido");
            req.setAttribute("mana.usuario:" + usuario.getCorreo(), usuario);
            var user = detalles.loadUserByUsername(usuario.getCorreo());
            if (!user.isEnabled())
                throw new BadCredentialsException("Token inválido");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            entry.commence(req, res, new BadCredentialsException("Token inválido"));
            return;
        }
        chain.doFilter(req, res);
    }
}
