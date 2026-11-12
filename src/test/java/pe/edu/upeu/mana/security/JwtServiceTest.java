package pe.edu.upeu.mana.security;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "cabecera.contenido.firma", "Y2xhdmU="})
    void rechazaConfiguracionInvalidaConMensajeClaro(String secret) {
        var error = assertThrows(IllegalArgumentException.class,
                () -> new JwtService(secret, Clock.systemUTC()));
        assertTrue(error.getMessage().contains("app.jwt.secret (JWT_SECRET)"));
        if (secret != null && !secret.isBlank()) {
            assertFalse(error.getMessage().contains(secret));
        }
    }

    @Test
    void aceptaClaveBase64De32Bytes() {
        assertDoesNotThrow(() -> new JwtService(
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", Clock.systemUTC()));
    }
}
