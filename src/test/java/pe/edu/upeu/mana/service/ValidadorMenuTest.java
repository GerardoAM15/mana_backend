package pe.edu.upeu.mana.service;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.mana.dto.consumo.PlatoCantidadRequestDTO;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidadorMenuTest {

    @Mock
    ProgramacionMenuRepository menus;

    @Mock
    MenuPlatoRepository relaciones;

    @Mock
    PlatoRepository platos;

    @Mock
    ComponenteReglaRepository componentes;

    @Mock
    ReglaCobroRepository reglas;

    @InjectMocks
    ValidadorMenu validador;

    ReglaCobro regla;

    LocalDate hoy = LocalDate.parse("2026-11-02");

    @BeforeEach
    void preparar() {
        regla = new ReglaCobro();
        regla.setId(4L);
        regla.setServicio("D");
        var menu = new ProgramacionMenu();
        menu.setId(1L);
        when(menus.findByLocalIdAndFechaAndServicio(1L, hoy, "D")).thenReturn(Optional.of(menu));
        var a = new MenuPlato();
        a.setPlatoId(2L);
        var b = new MenuPlato();
        b.setPlatoId(3L);
        when(relaciones.findByMenuId(1L)).thenReturn(List.of(a, b));
    }

    private void composicion() {
        var pan = new Plato();
        pan.setId(2L);
        pan.setCategoria("PAN");
        when(platos.findById(2L)).thenReturn(Optional.of(pan));
        var bebida = new Plato();
        bebida.setId(3L);
        bebida.setCategoria("BEBIDA_DESAYUNO");
        when(platos.findById(3L)).thenReturn(Optional.of(bebida));
        var cp = new ComponenteRegla();
        cp.setCategoria("PAN");
        cp.setCantidad(2L);
        var cb = new ComponenteRegla();
        cb.setCategoria("BEBIDA_DESAYUNO");
        cb.setCantidad(1L);
        when(componentes.findByReglaId(4L)).thenReturn(List.of(cp, cb));
    }

    @Test
    void exigeLaCantidadExactaDeCadaCategoria() {
        composicion();
        assertThrows(ReglaNegocioException.class, () -> validador.validar(1L, hoy, "D", regla, List.of(new PlatoCantidadRequestDTO(2L, 1L), new PlatoCantidadRequestDTO(3L, 1L))));
    }

    @Test
    void aceptaUnLiquidoYDosPanes() {
        composicion();
        assertEquals(Set.of(2L, 3L), validador.validar(1L, hoy, "D", regla, List.of(new PlatoCantidadRequestDTO(2L, 2L), new PlatoCantidadRequestDTO(3L, 1L))).keySet());
    }

    @Test
    void rechazaPlatoFueraDeLaProgramacion() {
        assertThrows(ReglaNegocioException.class, () -> validador.validar(1L, hoy, "D", regla, List.of(new PlatoCantidadRequestDTO(99L, 1L))));
        verifyNoInteractions(platos, componentes);
    }
}
