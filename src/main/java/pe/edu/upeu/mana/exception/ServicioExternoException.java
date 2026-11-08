package pe.edu.upeu.mana.exception;

// Un servicio externo (APIsPERU) no respondió o no está configurado: 503.
public class ServicioExternoException extends RuntimeException {

    public ServicioExternoException(String mensaje) {
        super(mensaje);
    }
}
