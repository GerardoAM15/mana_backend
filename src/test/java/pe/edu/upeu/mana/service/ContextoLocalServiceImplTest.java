package pe.edu.upeu.mana.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.edu.upeu.mana.entity.Empleado;
import pe.edu.upeu.mana.entity.Usuario;
import pe.edu.upeu.mana.enums.RolUsuario;
import pe.edu.upeu.mana.repository.EmpleadoRepository;
import pe.edu.upeu.mana.repository.UsuarioRepository;
import pe.edu.upeu.mana.service.impl.ContextoLocalServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextoLocalServiceImplTest {
    @Mock UsuarioRepository usuarios;
    @Mock EmpleadoRepository empleados;

    @AfterEach void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @EnumSource(value = RolUsuario.class, names = {"DUENO", "ADMINISTRADOR"})
    void duenoYAdministradorPuedenConsultarOtrosLocales(RolUsuario rol) {
        var service = preparar(rol);
        assertEquals(1L, service.localId(null));
        assertEquals(2L, service.localId(2L));
        assertDoesNotThrow(() -> service.validarLocal(2L));
    }

    @Test void cajeroSoloPuedeOperarEnSuLocal() {
        var service = preparar(RolUsuario.CAJERO);
        assertEquals(1L, service.localId(null));
        assertEquals(1L, service.localId(1L));
        assertDoesNotThrow(() -> service.validarLocal(1L));
        assertThrows(AccessDeniedException.class, () -> service.localId(2L));
        assertThrows(AccessDeniedException.class, () -> service.validarLocal(2L));
    }

    private ContextoLocalServiceImpl preparar(RolUsuario rol) {
        var usuario = new Usuario();
        usuario.setRol(rol);
        usuario.setEmpleadoId(10L);
        var empleado = new Empleado();
        empleado.setLocalId(1L);
        when(usuarios.findByCorreoIgnoreCase("actor@example.invalid")).thenReturn(Optional.of(usuario));
        when(empleados.findById(10L)).thenReturn(Optional.of(empleado));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("actor@example.invalid", null, List.of()));
        return new ContextoLocalServiceImpl(usuarios, empleados);
    }
}
