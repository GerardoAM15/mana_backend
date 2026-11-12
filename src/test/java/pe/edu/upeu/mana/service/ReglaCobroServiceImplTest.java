package pe.edu.upeu.mana.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.mana.dto.menu.*;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.mapper.ReglaCobroMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.impl.ReglaCobroServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReglaCobroServiceImplTest {
    private static final String MENSAJE = "Ya existe un menú activo para ese servicio; desactívelo primero";

    @Mock ReglaCobroMapper mapper;
    @Mock ReglaCobroRepository repository;
    @Mock ComponenteReglaRepository relaciones;
    @Mock ServicioRepository servicios;
    @Mock CategoriaPlatoRepository categorias;
    @InjectMocks ReglaCobroServiceImpl service;

    @Test
    void crearRechazaUnSegundoMenuActivoTrasBloquearElServicio() {
        servicio("D");
        when(repository.existsByServicioAndActivoTrueAndIdNot("D", -1L)).thenReturn(true);

        var error = assertThrows(ReglaNegocioException.class, () -> service.crear(request("D", true)));

        assertEquals(MENSAJE, error.getMessage());
        var orden = inOrder(servicios, repository);
        orden.verify(servicios).buscarParaActualizar("D");
        orden.verify(repository).existsByServicioAndActivoTrueAndIdNot("D", -1L);
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(relaciones);
    }

    @Test
    void activarRechazaSiYaExisteOtraReglaActiva() {
        servicio("D");
        when(repository.existsByServicioAndActivoTrueAndIdNot("D", 2L)).thenReturn(true);

        var error = assertThrows(ReglaNegocioException.class, () -> service.actualizar(2L, request("D", true)));

        assertEquals(MENSAJE, error.getMessage());
        verifyNoInteractions(relaciones);
        verify(repository, never()).flush();
    }

    @Test
    void cambiarServicioRechazaUnMenuActivoEnElDestino() {
        servicio("A");
        when(repository.existsByServicioAndActivoTrueAndIdNot("A", 2L)).thenReturn(true);

        assertEquals(MENSAJE, assertThrows(ReglaNegocioException.class,
                () -> service.actualizar(2L, request("A", true))).getMessage());
        verifyNoInteractions(relaciones);
    }

    @Test
    void actualizarLaPropiaReglaActivaNoLaConsideraDuplicada() {
        servicio("D");
        categoria();
        var regla = new ReglaCobro();
        regla.setId(2L);
        regla.setServicio("D");
        when(repository.buscarParaActualizar(2L)).thenReturn(Optional.of(regla));

        assertDoesNotThrow(() -> service.actualizar(2L, request("D", true)));

        verify(repository).existsByServicioAndActivoTrueAndIdNot("D", 2L);
        assertTrue(regla.getActivo());
        assertEquals(new BigDecimal("4.50"), regla.getPrecioCombo());
        verify(repository).flush();
    }

    @Test
    void permiteCrearReglasInactivasParaUnServicioConMenuActivo() {
        servicio("D");
        categoria();
        when(repository.saveAndFlush(any())).thenAnswer(i -> {
            ReglaCobro regla = i.getArgument(0);
            regla.setId(3L);
            return regla;
        });

        assertDoesNotThrow(() -> service.crear(request("D", false)));

        verify(repository, never()).existsByServicioAndActivoTrueAndIdNot(anyString(), anyLong());
        verify(repository).saveAndFlush(argThat(r -> !r.getActivo()));
    }

    private void servicio(String codigo) {
        var servicio = new Servicio();
        servicio.setCodigo(codigo);
        when(servicios.buscarParaActualizar(codigo)).thenReturn(Optional.of(servicio));
    }

    private void categoria() {
        when(categorias.findById("PAN")).thenReturn(Optional.of(new CategoriaPlato()));
    }

    private ReglaCobroRequestDTO request(String servicio, boolean activo) {
        return new ReglaCobroRequestDTO("Menú de prueba", servicio, new BigDecimal("4.50"),
                BigDecimal.ZERO, activo, List.of(new ComponenteReglaRequestDTO("PAN", 2L)));
    }
}
