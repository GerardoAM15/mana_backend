package pe.edu.upeu.mana.security;

import java.util.*;
import static pe.edu.upeu.mana.enums.Permiso.*;
import pe.edu.upeu.mana.enums.Permiso;

public final class PermisosPorRol {

    private PermisosPorRol() {
    }

    private static final Map<String, Set<Permiso>> MATRIZ = Map.of("ADMINISTRADOR", Set.copyOf(EnumSet.allOf(Permiso.class)), "DUENO", Set.of(CLIENTE_LEER, CONTRATO_LEER, CAJA_OPERAR, CAJA_SUPERVISAR, MENU_LEER, PLAN_GESTIONAR, CALENDARIO_GESTIONAR, INVENTARIO_GESTIONAR, GASTO_GESTIONAR, REPORTE_VER), "CAJERO", Set.of(CLIENTE_LEER, CLIENTE_ESCRIBIR, CONTRATO_LEER, CONSUMO_REGISTRAR, VENTA_REGISTRAR, CAJA_OPERAR, MENU_LEER));

    public static List<String> permisos(String rol) {
        return MATRIZ.getOrDefault(rol, Set.of()).stream().map(Enum::name).sorted().toList();
    }
}
