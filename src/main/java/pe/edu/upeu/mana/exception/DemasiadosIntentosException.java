package pe.edu.upeu.mana.exception;
public class DemasiadosIntentosException extends RuntimeException {
    public DemasiadosIntentosException() { super("Demasiados intentos; intente más tarde"); }
}
