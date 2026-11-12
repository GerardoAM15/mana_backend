package pe.edu.upeu.mana.service;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.dto.menu.*;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.mapper.MenuMapper;
import pe.edu.upeu.mana.service.impl.MenuServiceImpl;
import pe.edu.upeu.mana.service.service.ContextoLocalService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuAtencionTest {
    @Mock ProgramacionMenuRepository menus;
    @Mock MenuPlatoRepository relaciones;
    @Mock PlatoRepository platos;
    @Mock ServicioRepository servicios;
    @Mock ServicioDiaAtencionRepository dias;
    @Mock DiaNoAtencionRepository cierres;
    @Mock ContextoLocalService contexto;
    @Mock MenuMapper mapper;
    final LocalDate viernes=LocalDate.of(2026,11,6);
    private MenuServiceImpl preparar() {
        when(contexto.localId(null)).thenReturn(1L);
        return new MenuServiceImpl(menus,relaciones,platos,servicios,contexto,mapper,new ValidadorAtencion(servicios,dias,cierres));
    }
    @Test void noPermiteGuardarCenaEnViernes() {
        var service=preparar();when(servicios.findById("C")).thenReturn(Optional.of(new Servicio()));
        assertTrue(assertThrows(ReglaNegocioException.class,()->service.guardar(new MenuRequestDTO(viernes,"C",Set.of()))).getMessage().contains("viernes"));
        verifyNoInteractions(menus,platos,relaciones);
    }
    @Test void noPermiteCopiarACenaEnViernes() {
        var service=preparar();when(servicios.findById("C")).thenReturn(Optional.of(new Servicio()));
        assertThrows(ReglaNegocioException.class,()->service.copiarACena(new CopiarCenaRequestDTO(viernes)));
        verifyNoInteractions(menus,platos,relaciones);
    }
    @Test void clonarOmiteElServicioNoAtendidoEnDestino() {
        var service=preparar();var origen=viernes.minusDays(1);
        var cena=new ProgramacionMenu();cena.setId(1L);cena.setServicio("C");
        when(menus.filtrar(1L,origen,origen,null)).thenReturn(List.of(cena));
        when(servicios.findById("C")).thenReturn(Optional.of(new Servicio()));
        assertTrue(service.clonar(new ClonarMenuRequestDTO(origen,viernes,null)).isEmpty());
        verify(menus,never()).saveAndFlush(any());verifyNoInteractions(relaciones,platos);
    }
}
