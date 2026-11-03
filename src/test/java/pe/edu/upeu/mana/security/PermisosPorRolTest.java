package pe.edu.upeu.mana.security;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pe.edu.upeu.mana.enums.Permiso;

class PermisosPorRolTest {

    @Test
    void administradorTieneTodosLosPermisos() {
        assertEquals(Arrays.stream(Permiso.values()).map(Enum::name).sorted().toList(), PermisosPorRol.permisos("ADMINISTRADOR"));
    }

    @Test
    void duenoSupervisaSinRegistrarContratosNiVentas() {
        assertEquals(java.util.Set.of("CLIENTE_LEER", "CONTRATO_LEER", "CAJA_OPERAR", "CAJA_SUPERVISAR", "MENU_LEER", "PLAN_GESTIONAR", "CALENDARIO_GESTIONAR", "INVENTARIO_GESTIONAR", "GASTO_GESTIONAR", "REPORTE_VER"), java.util.Set.copyOf(PermisosPorRol.permisos("DUENO")));
    }

    @Test
    void cajeroOperaSinAnularNiSupervisar() {
        assertEquals(java.util.Set.of("CLIENTE_LEER", "CLIENTE_ESCRIBIR", "CONTRATO_LEER", "CONSUMO_REGISTRAR", "VENTA_REGISTRAR", "CAJA_OPERAR", "MENU_LEER"), java.util.Set.copyOf(PermisosPorRol.permisos("CAJERO")));
    }

    @Test
    void rolDesconocidoNoTienePermisos() {
        assertTrue(PermisosPorRol.permisos("DESCONOCIDO").isEmpty());
    }
}
