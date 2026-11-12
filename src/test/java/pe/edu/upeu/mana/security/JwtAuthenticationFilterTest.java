package pe.edu.upeu.mana.security;

import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import pe.edu.upeu.mana.config.*;
import pe.edu.upeu.mana.entity.Usuario;
import pe.edu.upeu.mana.repository.UsuarioRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneId.of("America/Lima"));

    private final JwtService jwt = new JwtService("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", clock);

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);

    private final UsuarioDetailsService detalles = mock(UsuarioDetailsService.class);

    private final RestAuthenticationEntryPoint entry = mock(RestAuthenticationEntryPoint.class);

    private final JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(jwt, detalles, usuarios, entry);

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cambioDeVersionInvalidaElToken() throws Exception {
        var usuario = usuario();
        String token = jwt.generarToken(usuario);
        usuario.setTokenVersion(1L);
        when(usuarios.findByCorreoIgnoreCase(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        var cadena = new MockFilterChain();
        filtro.doFilter(request(token), new MockHttpServletResponse(), cadena);
        assertNull(cadena.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(entry).commence(any(), any(), any());
    }

    @Test
    void usuarioInactivoNoPuedeUsarTokenAnterior() throws Exception {
        var usuario = usuario();
        String token = jwt.generarToken(usuario);
        usuario.setActivo(false);
        when(usuarios.findByCorreoIgnoreCase(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        filtro.doFilter(request(token), new MockHttpServletResponse(), new MockFilterChain());
        verify(entry).commence(any(), any(), any());
        verifyNoInteractions(detalles);
    }

    @Test
    void tokenVigenteCargaLosPermisosActuales() throws Exception {
        var usuario = usuario();
        when(usuarios.findByCorreoIgnoreCase(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(detalles.loadUserByUsername(usuario.getCorreo())).thenReturn(User.withUsername(usuario.getCorreo()).password("sin-uso").authorities("MENU_LEER").build());
        var cadena = new MockFilterChain();
        filtro.doFilter(request(jwt.generarToken(usuario)), new MockHttpServletResponse(), cadena);
        assertNotNull(cadena.getRequest());
        assertEquals("MENU_LEER", SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
        verifyNoInteractions(entry);
    }

    @Test
    void tokenVencidoEsRechazado() {
        String token = jwt.generarToken(usuario());
        var despues = new JwtService("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", Clock.offset(clock, Duration.ofHours(2)));
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> despues.validar(token));
    }

    private Usuario usuario() {
        var usuario = new Usuario();
        usuario.setCorreo("prueba@example.invalid");
        usuario.setActivo(true);
        usuario.setTokenVersion(0L);
        return usuario;
    }

    private MockHttpServletRequest request(String token) {
        var req = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        req.setServletPath("/api/v1/auth/me");
        req.addHeader("Authorization", "Bearer " + token);
        return req;
    }
}
