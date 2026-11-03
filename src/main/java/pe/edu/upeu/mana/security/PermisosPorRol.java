package pe.edu.upeu.mana.security;

import java.util.*;
import static pe.edu.upeu.mana.enums.Permiso.*;
import pe.edu.upeu.mana.enums.Permiso;
import pe.edu.upeu.mana.enums.RolUsuario;

public final class PermisosPorRol {

    private PermisosPorRol() {
    }

    private static final Map<String, Set<Permiso>> MATRIZ = Map.of(
            "DUENO", Set.copyOf(EnumSet.allOf(Permiso.class)),
            "ADMINISTRADOR", Set.copyOf(EnumSet.complementOf(EnumSet.of(LOCAL_GESTIONAR))),
            "CAJERO", Set.of(CLIENTE_LEER, CLIENTE_ESCRIBIR, CONTRATO_LEER, CONSUMO_REGISTRAR, VENTA_REGISTRAR, CAJA_OPERAR, MENU_LEER));

    public static boolean esAdministradorODueno(RolUsuario rol) {
        return rol == RolUsuario.DUENO || rol == RolUsuario.ADMINISTRADOR;
    }

    public static List<String> permisos(String rol) {
        return MATRIZ.getOrDefault(rol, Set.of()).stream().map(Enum::name).sorted().toList();
    }
}
