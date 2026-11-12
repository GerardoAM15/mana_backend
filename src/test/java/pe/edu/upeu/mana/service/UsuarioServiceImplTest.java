package pe.edu.upeu.mana.service;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.mana.dto.usuario.*;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.RolUsuario;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
import pe.edu.upeu.mana.mapper.UsuarioGestionMapper;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.impl.UsuarioServiceImpl;
import pe.edu.upeu.mana.service.service.ContextoLocalService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {
    @Mock UsuarioRepository usuarios;
    @Mock EmpleadoRepository empleados;
    @Mock PersonaRepository personas;
    @Mock LocalRepository locales;
    @Mock RolRepository roles;
    @Mock PasswordEncoder encoder;
    @Mock ContextoLocalService contexto;
    UsuarioServiceImpl service;
    Usuario actual;
    Usuario objetivo;
    Empleado empleado;

    @BeforeEach void preparar() {
        service = new UsuarioServiceImpl(usuarios, empleados, personas, locales, roles, encoder, new UsuarioGestionMapper(), contexto);
        actual = usuario(1L, RolUsuario.DUENO);
        objetivo = usuario(2L, RolUsuario.CAJERO);
        empleado = new Empleado();
        empleado.setId(20L); empleado.setLocalId(1L); empleado.setDni("87654321");
        empleado.setNombre("Nombre"); empleado.setApellido("Apellido"); empleado.setCargo("Empleado");
    }

    @ParameterizedTest @EnumSource(value=RolUsuario.class,names={"DUENO","ADMINISTRADOR"})
    void administradorNoPuedeCrearUsuariosConRolSuperior(RolUsuario asignado) {
        actual.setRol(RolUsuario.ADMINISTRADOR); gestion();
        assertDenegado(() -> service.crear(request(asignado,true)));
        verifyNoInteractions(empleados,personas,locales,encoder);
        verify(usuarios,never()).saveAndFlush(any());
    }

    @ParameterizedTest @EnumSource(RolUsuario.class)
    void duenoPuedeCrearTodosLosRoles(RolUsuario asignado) {
        crearValido(asignado);
    }

    @Test void administradorPuedeCrearCajeros() {
        actual.setRol(RolUsuario.ADMINISTRADOR); crearValido(RolUsuario.CAJERO);
    }

    private void crearValido(RolUsuario asignado) {
        gestion(); datosValidos(asignado);
        var r=request(asignado,true);r.setClave("ClaveSoloParaTest1");
        when(encoder.encode(r.getClave())).thenReturn("hash-de-prueba");
        when(empleados.saveAndFlush(any())).thenAnswer(i -> {Empleado e=i.getArgument(0);e.setId(20L);return e;});
        when(usuarios.saveAndFlush(any())).thenAnswer(i -> {Usuario u=i.getArgument(0);u.setId(2L);return u;});
        var respuesta=service.crear(r);
        assertEquals(asignado,respuesta.getRol());assertTrue(respuesta.isGestionable());
        verify(usuarios).saveAndFlush(argThat(u -> u.getRol()==asignado && u.getTokenVersion()==0));
    }

    @ParameterizedTest @EnumSource(value=RolUsuario.class,names={"DUENO","ADMINISTRADOR"})
    void administradorNoPuedeEditarUsuariosDeRolSuperior(RolUsuario rolObjetivo) {
        actual.setRol(RolUsuario.ADMINISTRADOR);objetivo.setRol(rolObjetivo);gestion();bloquearObjetivo();
        assertDenegado(() -> service.actualizar(2L,request(RolUsuario.CAJERO,true)));
        verifyNoInteractions(empleados,personas,encoder);assertEquals(7L,objetivo.getTokenVersion());
    }

    @ParameterizedTest @EnumSource(value=RolUsuario.class,names={"DUENO","ADMINISTRADOR"})
    void administradorNoPuedeAsignarUnRolSuperiorAlEditarUnCajero(RolUsuario asignado) {
        actual.setRol(RolUsuario.ADMINISTRADOR);gestion();bloquearObjetivo();
        assertDenegado(() -> service.actualizar(2L,request(asignado,true)));
        verifyNoInteractions(empleados,personas,encoder);
    }

    @ParameterizedTest @EnumSource(value=RolUsuario.class,names={"DUENO","ADMINISTRADOR"})
    void administradorNoPuedeDesactivarUsuariosDeRolSuperior(RolUsuario rolObjetivo) {
        actual.setRol(RolUsuario.ADMINISTRADOR);objetivo.setRol(rolObjetivo);gestion();bloquearObjetivo();
        assertDenegado(() -> service.eliminar(2L));assertTrue(objetivo.getActivo());
    }

    @ParameterizedTest @EnumSource(value=RolUsuario.class,names={"DUENO","ADMINISTRADOR"})
    void administradorNoPuedeCambiarElRolDeUnUsuarioSuperior(RolUsuario rolObjetivo) {
        actual.setRol(RolUsuario.ADMINISTRADOR);objetivo.setRol(rolObjetivo);gestion();bloquearObjetivo();
        assertDenegado(() -> service.cambiarRol(2L,new RolRequestDTO(RolUsuario.CAJERO)));
        assertEquals(rolObjetivo,objetivo.getRol());
    }

    @ParameterizedTest @EnumSource(value=RolUsuario.class,names={"DUENO","ADMINISTRADOR"})
    void administradorNoPuedePromoverUnCajero(RolUsuario asignado) {
        actual.setRol(RolUsuario.ADMINISTRADOR);gestion();bloquearObjetivo();
        assertDenegado(() -> service.cambiarRol(2L,new RolRequestDTO(asignado)));
        assertEquals(RolUsuario.CAJERO,objetivo.getRol());
    }

    @Test void administradorPuedeEditarYDesactivarUnCajeroDesdePut() {
        actual.setRol(RolUsuario.ADMINISTRADOR);gestion();bloquearObjetivo();datosValidos(RolUsuario.CAJERO);
        when(empleados.buscarParaActualizar(20L)).thenReturn(Optional.of(empleado));
        var respuesta=service.actualizar(2L,request(RolUsuario.CAJERO,false));
        assertFalse(respuesta.getActivo());assertTrue(respuesta.isGestionable());assertEquals(8L,objetivo.getTokenVersion());
    }

    @Test void administradorPuedeDesactivarUnCajero() {
        actual.setRol(RolUsuario.ADMINISTRADOR);gestion();bloquearObjetivo();
        service.eliminar(2L);assertFalse(objetivo.getActivo());assertEquals(8L,objetivo.getTokenVersion());
    }

    @Test void administradorPuedeAsignarCajeroAUnCajeroSinInvalidarTokenInnecesariamente() {
        actual.setRol(RolUsuario.ADMINISTRADOR);gestion();bloquearObjetivo();lectura();rolValido(RolUsuario.CAJERO);
        assertTrue(service.cambiarRol(2L,new RolRequestDTO(RolUsuario.CAJERO)).isGestionable());
        assertEquals(7L,objetivo.getTokenVersion());
    }

    @ParameterizedTest @EnumSource(RolUsuario.class)
    void duenoPuedeEditarCualquierRol(RolUsuario rolObjetivo) {
        objetivo.setRol(rolObjetivo);gestion();bloquearObjetivo();datosValidos(rolObjetivo);
        when(empleados.buscarParaActualizar(20L)).thenReturn(Optional.of(empleado));
        var respuesta=service.actualizar(2L,request(rolObjetivo,true));
        assertEquals(rolObjetivo,respuesta.getRol());assertTrue(respuesta.isGestionable());
    }

    @ParameterizedTest @EnumSource(RolUsuario.class)
    void duenoPuedeCambiarElRolDeCualquierOtroUsuario(RolUsuario rolObjetivo) {
        objetivo.setRol(rolObjetivo);gestion();bloquearObjetivo();lectura();
        var nuevo=rolObjetivo==RolUsuario.DUENO?RolUsuario.ADMINISTRADOR:RolUsuario.DUENO;
        rolValido(nuevo);
        if(rolObjetivo==RolUsuario.DUENO)when(usuarios.countByRolAndActivoTrue(RolUsuario.DUENO)).thenReturn(2L);
        var respuesta=service.cambiarRol(2L,new RolRequestDTO(nuevo));
        assertEquals(nuevo,respuesta.getRol());assertEquals(8L,objetivo.getTokenVersion());
    }

    @ParameterizedTest @EnumSource(RolUsuario.class)
    void duenoPuedeDesactivarCualquierOtroUsuarioSiQuedaUnDuenoActivo(RolUsuario rolObjetivo) {
        objetivo.setRol(rolObjetivo);gestion();bloquearObjetivo();
        if(rolObjetivo==RolUsuario.DUENO)when(usuarios.countByRolAndActivoTrue(RolUsuario.DUENO)).thenReturn(2L);
        service.eliminar(2L);assertFalse(objetivo.getActivo());assertEquals(8L,objetivo.getTokenVersion());
    }

    @ParameterizedTest @ValueSource(strings={"eliminar","desactivar","cambiarRol","actualizarRol"})
    void noPermiteEliminarAlUltimoDuenoActivo(String operacion) {
        objetivo.setRol(RolUsuario.DUENO);objetivo.setId(1L);gestion();bloquearObjetivo();
        when(usuarios.countByRolAndActivoTrue(RolUsuario.DUENO)).thenReturn(1L);
        var error=assertThrows(ReglaNegocioException.class,()->operar(operacion));
        assertEquals("Debe existir al menos un dueño activo",error.getMessage());
        assertTrue(objetivo.getActivo());assertEquals(RolUsuario.DUENO,objetivo.getRol());assertEquals(7L,objetivo.getTokenVersion());
        verifyNoInteractions(empleados,personas,encoder);
        var orden=inOrder(roles,usuarios);orden.verify(roles).buscarParaActualizar("DUENO");orden.verify(usuarios).buscarParaActualizar(1L);
    }

    @ParameterizedTest @ValueSource(strings={"eliminar","desactivar","cambiarRol","actualizarRol"})
    void noPermiteCambiarElRolNiDesactivarLaPropiaCuentaAunqueHayaOtroDueno(String operacion) {
        objetivo.setRol(RolUsuario.DUENO);objetivo.setId(1L);gestion();bloquearObjetivo();
        when(usuarios.countByRolAndActivoTrue(RolUsuario.DUENO)).thenReturn(2L);
        assertDenegado(()->operar(operacion));assertTrue(objetivo.getActivo());assertEquals(7L,objetivo.getTokenVersion());
    }

    @Test void tampocoPermiteAsignarseElMismoRolDesdeCambiarRol() {
        objetivo.setRol(RolUsuario.DUENO);objetivo.setId(1L);gestion();bloquearObjetivo();
        assertDenegado(()->service.cambiarRol(1L,new RolRequestDTO(RolUsuario.DUENO)));
    }

    @Test void puedeEditarSusDatosPersonalesSinCambiarRolNiEstado() {
        objetivo.setRol(RolUsuario.DUENO);objetivo.setId(1L);gestion();bloquearObjetivo();datosValidos(RolUsuario.DUENO);
        when(empleados.buscarParaActualizar(20L)).thenReturn(Optional.of(empleado));
        var respuesta=service.actualizar(1L,request(RolUsuario.DUENO,true));
        assertTrue(respuesta.isGestionable());assertEquals(RolUsuario.DUENO,respuesta.getRol());assertTrue(respuesta.getActivo());
    }

    @ParameterizedTest @ValueSource(strings={"crear","eliminar","desactivar","cambiarRol","actualizarRol"})
    void cajeroNoPuedeGestionarUsuarios(String operacion) {
        actual.setRol(RolUsuario.CAJERO);gestion();
        assertDenegado(()->operar(operacion));verifyNoInteractions(usuarios,empleados,personas,encoder);
    }

    @ParameterizedTest
    @CsvSource({"DUENO,DUENO,false,true","DUENO,ADMINISTRADOR,false,true","DUENO,CAJERO,false,true",
            "ADMINISTRADOR,DUENO,false,false","ADMINISTRADOR,ADMINISTRADOR,false,false","ADMINISTRADOR,CAJERO,false,true",
            "CAJERO,DUENO,false,false","CAJERO,ADMINISTRADOR,false,false","CAJERO,CAJERO,false,false",
            "DUENO,DUENO,true,true","ADMINISTRADOR,ADMINISTRADOR,true,false","CAJERO,CAJERO,true,false"})
    void gestionableReflejaLaJerarquiaIncluidaLaEdicionDelPerfilDelDueno(RolUsuario rolActual,RolUsuario rolObjetivo,boolean propio,boolean esperado) {
        actual.setRol(rolActual);objetivo.setRol(rolObjetivo);if(propio)objetivo.setId(1L);
        when(contexto.usuario()).thenReturn(actual);when(usuarios.findById(objetivo.getId())).thenReturn(Optional.of(objetivo));lectura();
        assertEquals(esperado,service.buscar(objetivo.getId()).isGestionable());
    }

    @Test void listarCalculaGestionableSegunElUsuarioActual() {
        actual.setRol(RolUsuario.ADMINISTRADOR);when(contexto.usuario()).thenReturn(actual);lectura();
        when(usuarios.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(usuario(2L,RolUsuario.DUENO),usuario(3L,RolUsuario.ADMINISTRADOR),usuario(4L,RolUsuario.CAJERO)));
        assertEquals(List.of(false,false,true),service.listar().stream().map(UsuarioResponseDTO::isGestionable).toList());
    }

    private void operar(String operacion) {
        switch(operacion) {
            case "crear" -> service.crear(request(RolUsuario.CAJERO,true));
            case "eliminar" -> service.eliminar(objetivo.getId());
            case "desactivar" -> service.actualizar(objetivo.getId(),request(objetivo.getRol(),false));
            case "cambiarRol" -> service.cambiarRol(objetivo.getId(),new RolRequestDTO(RolUsuario.ADMINISTRADOR));
            case "actualizarRol" -> service.actualizar(objetivo.getId(),request(RolUsuario.ADMINISTRADOR,true));
            default -> throw new IllegalArgumentException(operacion);
        }
    }

    private void assertDenegado(org.junit.jupiter.api.function.Executable operacion) {
        assertEquals("No tiene permiso para gestionar este usuario",assertThrows(AccessDeniedException.class,operacion).getMessage());
    }

    private void gestion() {when(roles.buscarParaActualizar("DUENO")).thenReturn(Optional.of(new Rol()));when(contexto.usuario()).thenReturn(actual);}
    private void bloquearObjetivo() {when(usuarios.buscarParaActualizar(objetivo.getId())).thenReturn(Optional.of(objetivo));}
    private void lectura() {when(empleados.findById(20L)).thenReturn(Optional.of(empleado));var local=new Local();local.setId(1L);local.setNombre("Local");when(locales.findById(1L)).thenReturn(Optional.of(local));}
    private void rolValido(RolUsuario rol) {when(roles.findById(rol.name())).thenReturn(Optional.of(new Rol()));}
    private void datosValidos(RolUsuario rol) {lectura();rolValido(rol);}
    private Usuario usuario(Long id,RolUsuario rol) {var u=new Usuario();u.setId(id);u.setEmpleadoId(20L);u.setRol(rol);u.setActivo(true);u.setCorreo("persona@example.invalid");u.setTokenVersion(7L);return u;}
    private UsuarioRequestDTO request(RolUsuario rol,boolean activo) {return new UsuarioRequestDTO("87654321","Nombre editado","Apellido",null,1L,"Empleado","persona@example.invalid",null,rol,activo);}
}
