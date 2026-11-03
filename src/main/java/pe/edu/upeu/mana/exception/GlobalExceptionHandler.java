package pe.edu.upeu.mana.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import pe.edu.upeu.mana.exception.dto.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public GlobalExceptionHandler(java.time.Clock clock) {
        this.clock = clock;
    }

    private final java.time.Clock clock;

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponseDTO> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var campos = new LinkedHashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        log.warn("Validacion rechazada | ruta={} | campos={}", req.getRequestURI(), campos.keySet());
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(ahora(), 400, "Bad Request", "Datos invalidos", req.getRequestURI(), campos));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorResponseDTO> noEncontrado(RuntimeException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponseDTO> rutaNoEncontrada(NoResourceFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, "Ruta no encontrada", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponseDTO> metodoNoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP no permitido", req);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<ErrorResponseDTO> conflicto(RuntimeException ex, HttpServletRequest req) {
        log.warn("Regla rechazada | ruta={} | motivo={}", req.getRequestURI(), ex.getMessage());
        return error(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({ DatosInvalidosException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, MissingServletRequestParameterException.class })
    ResponseEntity<ErrorResponseDTO> invalido(Exception ex, HttpServletRequest req) {
        String mensaje = ex instanceof DatosInvalidosException ? ex.getMessage() : "Solicitud invalida: revise campos, tipos y fechas (yyyy-MM-dd)";
        log.warn("Solicitud rechazada | ruta={} | motivo={}", req.getRequestURI(), mensaje);
        return error(HttpStatus.BAD_REQUEST, mensaje, req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponseDTO> integridad(Exception ex, HttpServletRequest req) {
        log.warn("Conflicto de integridad o concurrencia | ruta={}", req.getRequestURI());
        return error(HttpStatus.CONFLICT, "Conflicto: dato duplicado, recurso relacionado o en uso; vuelva a consultar", req);
    }

    @ExceptionHandler({PessimisticLockingFailureException.class, org.springframework.dao.OptimisticLockingFailureException.class})
    ResponseEntity<ErrorResponseDTO> concurrencia(Exception ex,HttpServletRequest req) {
        return error(HttpStatus.CONFLICT,"Otro usuario modificó el registro; vuelva a consultar",req);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponseDTO> autenticacion(AuthenticationException ex, HttpServletRequest req) {
        return error(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponseDTO> denegado(AccessDeniedException ex, HttpServletRequest req) {
        return error(HttpStatus.FORBIDDEN, "No tiene permiso para esta operacion", req);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponseDTO> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado | ruta={}", req.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req);
    }

    private ResponseEntity<ErrorResponseDTO> error(HttpStatus status, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ErrorResponseDTO(ahora(), status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI(), null));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }
}
