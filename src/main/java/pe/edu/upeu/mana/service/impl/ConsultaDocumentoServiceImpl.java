package pe.edu.upeu.mana.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.mana.dto.consulta.DniResponseDTO;
import pe.edu.upeu.mana.dto.consulta.RucResponseDTO;
import pe.edu.upeu.mana.exception.DatosInvalidosException;
import pe.edu.upeu.mana.exception.RecursoNoEncontradoException;
import pe.edu.upeu.mana.repository.PersonaRepository;
import pe.edu.upeu.mana.security.LimitadorIntentos;
import pe.edu.upeu.mana.service.ApisPeruCliente;
import pe.edu.upeu.mana.service.service.ConsultaDocumentoService;

// Autocompletado por DNI/RUC. Primero busca en la BD (no gasta consultas);
// después en APIsPERU, con caché en memoria para no repetir la misma
// consulta (el plan gratuito da 1000 al mes). Si la API falla, el usuario
// ingresa los datos a mano: el registro nunca depende de ella.
@Service
public class ConsultaDocumentoServiceImpl implements ConsultaDocumentoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaDocumentoServiceImpl.class);

    private static final Duration VIGENCIA_CACHE = Duration.ofHours(12);

    private static final int MAXIMO_CACHE = 500;

    private record EnCache<T>(T valor, Instant vence) {
    }

    private final Map<String, EnCache<DniResponseDTO>> cacheDni = new ConcurrentHashMap<>();

    private final Map<String, EnCache<RucResponseDTO>> cacheRuc = new ConcurrentHashMap<>();

    private final PersonaRepository personas;

    private final ApisPeruCliente apisPeru;

    private final LimitadorIntentos limitador;

    private final Clock clock;

    public ConsultaDocumentoServiceImpl(PersonaRepository personas, ApisPeruCliente apisPeru, LimitadorIntentos limitador, Clock clock) {
        this.personas = personas;
        this.apisPeru = apisPeru;
        this.limitador = limitador;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DniResponseDTO consultarDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}"))
            throw new DatosInvalidosException("El DNI debe tener 8 dígitos");
        var local = personas.findByDni(dni);
        if (local.isPresent())
            return new DniResponseDTO(dni, local.get().getNombre(), local.get().getApellido(), "MANA", true);
        var guardado = leer(cacheDni, dni);
        if (guardado != null)
            return guardado;
        limitador.consultaDocumento(usuarioActual());
        var p = apisPeru.dni(dni).orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el DNI; ingrese los datos manualmente"));
        String apellido = (texto(p.apellidoPaterno()) + " " + texto(p.apellidoMaterno())).trim();
        var r = new DniResponseDTO(dni, recortar(texto(p.nombres()), 100), recortar(apellido, 100), "RENIEC", false);
        guardar(cacheDni, dni, r);
        log.info("DNI consultado en APIsPERU | dni=****{}", dni.substring(4));
        return r;
    }

    public RucResponseDTO consultarRuc(String ruc) {
        if (ruc == null || !ruc.matches("(10|15|17|20)\\d{9}"))
            throw new DatosInvalidosException("El RUC debe tener 11 dígitos y empezar con 10, 15, 17 o 20");
        var guardado = leer(cacheRuc, ruc);
        if (guardado != null)
            return guardado;
        limitador.consultaDocumento(usuarioActual());
        var e = apisPeru.ruc(ruc).orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el RUC; ingrese los datos manualmente"));
        String telefono = e.telefonos() == null ? null : e.telefonos().stream().filter(t -> t != null && !t.isBlank()).findFirst().map(t -> recortar(t.trim(), 20)).orElse(null);
        var r = new RucResponseDTO(ruc, recortar(texto(e.razonSocial()), 160), texto(e.direccion()), telefono, texto(e.estado()), texto(e.condicion()));
        guardar(cacheRuc, ruc, r);
        log.info("RUC consultado en APIsPERU | ruc={}", ruc);
        return r;
    }

    private <T> T leer(Map<String, EnCache<T>> cache, String clave) {
        var e = cache.get(clave);
        if (e == null)
            return null;
        if (!e.vence().isAfter(clock.instant())) {
            cache.remove(clave);
            return null;
        }
        return e.valor();
    }

    private <T> void guardar(Map<String, EnCache<T>> cache, String clave, T valor) {
        var ahora = clock.instant();
        if (cache.size() >= MAXIMO_CACHE) {
            cache.values().removeIf(e -> !e.vence().isAfter(ahora));
            if (cache.size() >= MAXIMO_CACHE)
                cache.clear();
        }
        cache.put(clave, new EnCache<>(valor, ahora.plus(VIGENCIA_CACHE)));
    }

    private String usuarioActual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "anonimo" : auth.getName();
    }

    private static String texto(String s) {
        return s == null ? "" : s.trim();
    }

    private static String recortar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
