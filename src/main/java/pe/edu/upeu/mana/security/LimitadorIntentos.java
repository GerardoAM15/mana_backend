package pe.edu.upeu.mana.security;
import java.time.*;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.exception.DemasiadosIntentosException;
@Component
public class LimitadorIntentos {
    private record Ventana(Instant vence, int intentos) {}
    private final ConcurrentHashMap<String, Ventana> ventanas = new ConcurrentHashMap<>();
    private final Clock clock;
    public LimitadorIntentos(Clock clock) { this.clock=clock; }
    private String correo(String correo) { return correo.trim().toLowerCase(Locale.ROOT); }
    private void limpiar() { var ahora=clock.instant(); ventanas.entrySet().removeIf(e -> !e.getValue().vence().isAfter(ahora)); }
    private void verificar(String clave, int limite) { var v=ventanas.get(clave); if(v!=null && v.intentos()>=limite) throw new DemasiadosIntentosException(); }
    private void sumar(String clave, Duration duracion) { ventanas.compute(clave,(k,v)-> v==null ? new Ventana(clock.instant().plus(duracion),1) : new Ventana(v.vence(),v.intentos()+1)); }
    public synchronized void iniciarLogin(String correo, String ip) {
        limpiar(); String c="login-correo:"+correo(correo), i="login-ip:"+ip;
        verificar(c,5); verificar(i,5); sumar(c,Duration.ofMinutes(5)); sumar(i,Duration.ofMinutes(5));
    }
    public synchronized void loginExitoso(String correo, String ip) { ventanas.remove("login-correo:"+correo(correo)); ventanas.remove("login-ip:"+ip); }
    public synchronized void recuperar(String correo) { limpiar(); String c="recuperar:"+correo(correo); verificar(c,3); sumar(c,Duration.ofMinutes(15)); }
    // Consultas DNI/RUC a APIsPERU: 30 por minuto por usuario, protege la cuota mensual.
    public synchronized void consultaDocumento(String usuario) { limpiar(); String c="consulta:"+correo(usuario); verificar(c,30); sumar(c,Duration.ofMinutes(1)); }
}
