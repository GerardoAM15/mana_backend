package pe.edu.upeu.mana.service;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.upeu.mana.controller.AuthController;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.auth.*;
import pe.edu.upeu.mana.exception.*;
import pe.edu.upeu.mana.mapper.UsuarioMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.security.*;
import pe.edu.upeu.mana.service.impl.AuthServiceImpl;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthCorreccionesTest {
    @Mock UsuarioRepository usuarios;
    @Mock EmpleadoRepository empleados;
    @Mock LocalRepository locales;
    @Mock TokenRestablecimientoRepository tokens;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @Mock UsuarioMapper mapper;
    @Mock ObjectProvider<JavaMailSender> correo;
    @Mock Environment environment;
    Clock clock=Clock.fixed(Instant.parse("2026-11-02T15:00:00Z"),ZoneId.of("America/Lima"));
    AuthServiceImpl service;
    LimitadorIntentos intentos;
    @BeforeEach void preparar() {
        intentos=new LimitadorIntentos(clock);
        service=new AuthServiceImpl(usuarios,empleados,locales,tokens,encoder,jwt,mapper,clock,correo,environment,"http://localhost:4200",intentos);
    }
    @Test void sextoLoginFallidoResponde429ConElJsonEstandar() throws Exception {
        var mvc=MockMvcBuilders.standaloneSetup(new AuthController(service)).setControllerAdvice(new GlobalExceptionHandler(clock)).build();
        for(int i=0;i<5;i++) mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                .content("{\"correo\":\"prueba@example.invalid\",\"clave\":\"incorrecta\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                .content("{\"correo\":\"prueba@example.invalid\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value("Demasiados intentos; intente más tarde"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }
    @Test void loginExitosoReiniciaLosContadores() {
        var u=new Usuario();u.setEmpleadoId(1L);u.setClaveHash("hash");u.setCorreo("prueba@example.invalid");
        var e=new Empleado();e.setLocalId(1L);
        when(usuarios.findByCorreoIgnoreCase(u.getCorreo())).thenReturn(Optional.of(u));
        when(encoder.matches("incorrecta","hash")).thenReturn(false);
        for(int i=0;i<4;i++)assertThrows(BadCredentialsException.class,()->service.login(new LoginRequestDTO(u.getCorreo(),"incorrecta")));
        when(encoder.matches("correcta","hash")).thenReturn(true);
        when(empleados.findById(1L)).thenReturn(Optional.of(e));when(locales.findById(1L)).thenReturn(Optional.of(new Local()));
        service.login(new LoginRequestDTO(u.getCorreo(),"correcta"));
        for(int i=0;i<5;i++)assertThrows(BadCredentialsException.class,()->service.login(new LoginRequestDTO(u.getCorreo(),"incorrecta")));
        assertThrows(DemasiadosIntentosException.class,()->service.login(new LoginRequestDTO(u.getCorreo(),"incorrecta")));
    }
    @Test void recuperarInvalidaTodosLosPendientesAntesDeGuardarElNuevo() {
        var u=new Usuario();u.setId(1L);
        when(usuarios.findByCorreoIgnoreCase("prueba@example.invalid")).thenReturn(Optional.of(u));
        when(usuarios.buscarParaActualizar(1L)).thenReturn(Optional.of(u));
        service.recuperar(new RecuperarClaveRequestDTO("prueba@example.invalid"));
        var orden=inOrder(usuarios,tokens);
        orden.verify(usuarios).buscarParaActualizar(1L);orden.verify(tokens).invalidarPendientes(1L);
        orden.verify(tokens).save(argThat(t -> !t.getUsado() && t.getTokenHash().length()==64 && t.getExpiraEn().equals(OffsetDateTime.now(clock).plusMinutes(5))));
    }
    @Test void recuperarLimitaInclusoCorreosInexistentes() {
        for(int i=0;i<3;i++)service.recuperar(new RecuperarClaveRequestDTO("no-existe@example.invalid"));
        assertThrows(DemasiadosIntentosException.class,()->service.recuperar(new RecuperarClaveRequestDTO("NO-EXISTE@example.invalid")));
    }
    @Test void restablecerRechazaUnTokenInvalidadoSinCambiarLaClave() {
        var usuario=new Usuario();usuario.setId(1L);usuario.setClaveHash("hash-anterior");usuario.setTokenVersion(3L);
        var anterior=new TokenRestablecimiento();anterior.setUsuarioId(1L);anterior.setUsado(true);
        anterior.setExpiraEn(OffsetDateTime.now(clock).plusMinutes(5));
        when(tokens.buscarUsuarioIdPorHash(anyString())).thenReturn(Optional.of(1L));
        when(usuarios.buscarParaActualizar(1L)).thenReturn(Optional.of(usuario));
        when(tokens.buscarPorHashParaActualizar(anyString())).thenReturn(Optional.of(anterior));
        assertThrows(DatosInvalidosException.class,()->service.restablecer(new RestablecerClaveRequestDTO("token-antiguo","ClaveSoloParaTest1")));
        assertEquals("hash-anterior",usuario.getClaveHash());assertEquals(3L,usuario.getTokenVersion());verifyNoInteractions(encoder);
    }
    @Test void limitesIndependientesPorCorreoEIpYExpiracion() {
        Clock reloj=mock(Clock.class);var ahora=Instant.parse("2026-11-02T15:00:00Z");when(reloj.instant()).thenReturn(ahora);
        var limite=new LimitadorIntentos(reloj);
        for(int i=0;i<5;i++)limite.iniciarLogin("uno@example.invalid","ip"+i);
        assertThrows(DemasiadosIntentosException.class,()->limite.iniciarLogin(" UNO@example.invalid ","otra"));
        for(int i=0;i<5;i++)limite.iniciarLogin("usuario"+i,"misma-ip");
        assertThrows(DemasiadosIntentosException.class,()->limite.iniciarLogin("otro","misma-ip"));
        for(int i=0;i<3;i++)limite.recuperar("correo");
        when(reloj.instant()).thenReturn(ahora.plusSeconds(300));
        assertDoesNotThrow(()->limite.iniciarLogin("uno@example.invalid","misma-ip"));
        assertThrows(DemasiadosIntentosException.class,()->limite.recuperar("correo"));
        when(reloj.instant()).thenReturn(ahora.plusSeconds(900));assertDoesNotThrow(()->limite.recuperar("correo"));
    }
}
