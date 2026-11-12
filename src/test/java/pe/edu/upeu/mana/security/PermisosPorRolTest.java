package pe.edu.upeu.mana.security;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pe.edu.upeu.mana.enums.Permiso;

class PermisosPorRolTest {

    @Test
    void duenoTieneTodosLosPermisos() {
        assertEquals(Arrays.stream(Permiso.values()).map(Enum::name).sorted().toList(), PermisosPorRol.permisos("DUENO"));
    }

    @Test
    void administradorTieneTodosExceptoGestionarLocales() {
        assertEquals(Arrays.stream(Permiso.values()).filter(p -> p != Permiso.LOCAL_GESTIONAR).map(Enum::name).sorted().toList(), PermisosPorRol.permisos("ADMINISTRADOR"));
    }

    @Test
    void cajeroOperaSinAnularNiSupervisar() {
        assertEquals(java.util.Set.of("COMPROBANTE_VER", "COMPROBANTE_EMITIR", "CLIENTE_LEER", "CLIENTE_ESCRIBIR", "CONTRATO_LEER", "CONSUMO_REGISTRAR", "VENTA_REGISTRAR", "CAJA_OPERAR", "MENU_LEER"), java.util.Set.copyOf(PermisosPorRol.permisos("CAJERO")));
    }

    @Test
    void rolDesconocidoNoTienePermisos() {
        assertTrue(PermisosPorRol.permisos("DESCONOCIDO").isEmpty());
    }
}
