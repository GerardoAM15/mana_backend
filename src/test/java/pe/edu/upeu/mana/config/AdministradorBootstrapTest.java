package pe.edu.upeu.mana.config;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.RolUsuario;
import pe.edu.upeu.mana.repository.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministradorBootstrapTest {
    @Mock UsuarioRepository usuarios;
    @Mock EmpleadoRepository empleados;
    @Mock PersonaRepository personas;
    @Mock LocalRepository locales;
    @Mock PasswordEncoder encoder;
    @Mock RolRepository roles;

    @Test void siExisteUnDuenoNoCreaOtroUsuario() {
        bloquear();when(usuarios.existsByRol(RolUsuario.DUENO)).thenReturn(true);
        bootstrap("ClaveSoloParaTest1").run(new DefaultApplicationArguments());
        verifyNoInteractions(empleados,personas,locales,encoder);
        verify(usuarios,never()).saveAndFlush(any());
    }

    @Test void sinDuenoCreaElUsuarioInicialComoDueno() {
        bloquear();local();when(encoder.encode("ClaveSoloParaTest1")).thenReturn("hash-de-prueba");
        when(empleados.saveAndFlush(any())).thenAnswer(i->{Empleado e=i.getArgument(0);e.setId(10L);return e;});
        bootstrap("ClaveSoloParaTest1").run(new DefaultApplicationArguments());
        var captor=ArgumentCaptor.forClass(Usuario.class);verify(usuarios).saveAndFlush(captor.capture());
        assertEquals(RolUsuario.DUENO,captor.getValue().getRol());assertTrue(captor.getValue().getActivo());assertEquals(0L,captor.getValue().getTokenVersion());assertEquals(10L,captor.getValue().getEmpleadoId());
        verify(usuarios).existsByRol(RolUsuario.DUENO);verify(usuarios,never()).existsByRol(RolUsuario.ADMINISTRADOR);
    }

    @Test void siFaltaLaClaveNoCreaNada() {
        bloquear();bootstrap("").run(new DefaultApplicationArguments());
        verify(usuarios,never()).saveAndFlush(any());verifyNoInteractions(empleados,personas,locales,encoder);
    }

    @Test void noPromueveAutomaticamenteUnaCuentaExistenteConElCorreoConfigurado() {
        bloquear();local();var anterior=new Usuario();anterior.setRol(RolUsuario.ADMINISTRADOR);
        when(usuarios.findByCorreoIgnoreCase("inicial@example.invalid")).thenReturn(Optional.of(anterior));
        bootstrap("ClaveSoloParaTest1").run(new DefaultApplicationArguments());
        assertEquals(RolUsuario.ADMINISTRADOR,anterior.getRol());verify(usuarios,never()).saveAndFlush(any());verifyNoInteractions(empleados,encoder);
    }

    private void bloquear(){when(roles.buscarParaActualizar("DUENO")).thenReturn(Optional.of(new Rol()));}
    private void local(){var l=new Local();l.setId(1L);when(locales.findAll()).thenReturn(List.of(l));}
    private AdministradorBootstrap bootstrap(String clave){return new AdministradorBootstrap(usuarios,empleados,personas,locales,encoder,roles,"inicial@example.invalid",clave,"87654321");}
}
