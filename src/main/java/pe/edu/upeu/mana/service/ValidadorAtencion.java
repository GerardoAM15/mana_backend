package pe.edu.upeu.mana.service;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.ServicioDiaAtencionId;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.exception.*;
@Component
public class ValidadorAtencion {
    private final ServicioRepository servicios;
    private final ServicioDiaAtencionRepository dias;
    private final DiaNoAtencionRepository cierres;
    public ValidadorAtencion(ServicioRepository servicios, ServicioDiaAtencionRepository dias, DiaNoAtencionRepository cierres) {
        this.servicios=servicios; this.dias=dias; this.cierres=cierres;
    }
    public boolean seAtiende(Long local, LocalDate fecha, String servicio) {
        return servicios.findById(servicio).map(s -> Boolean.TRUE.equals(s.getActivo())).orElse(false)
            && !cierres.existsByLocalIdAndFecha(local, fecha)
            && dias.existsById(new ServicioDiaAtencionId(servicio, fecha.getDayOfWeek().getValue()));
    }
    public void validar(Long local, LocalDate hoy, String servicio) {
        var s = servicios.findById(servicio).orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
        if (!Boolean.TRUE.equals(s.getActivo()))
            throw new ReglaNegocioException("El servicio está inactivo");
        if (cierres.existsByLocalIdAndFecha(local, hoy))
            throw new ReglaNegocioException("Hoy no hay atención en el local por feriado o cierre");
        if (!dias.existsById(new ServicioDiaAtencionId(servicio, hoy.getDayOfWeek().getValue())))
            throw new ReglaNegocioException("El servicio " + s.getNombre() + " no se atiende los " + hoy.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")));
    }

}
