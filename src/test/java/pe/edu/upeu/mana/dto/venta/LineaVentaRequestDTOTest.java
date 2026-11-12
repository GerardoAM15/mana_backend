package pe.edu.upeu.mana.dto.venta;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.mana.enums.TipoLineaVenta;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

class LineaVentaRequestDTOTest {
    @Test
    void ocultaFormaValidaEnJsonSinEliminarLaValidacionDeNegocio() {
        var linea = new LineaVentaRequestDTO(TipoLineaVenta.BEBIDA, 1L, 1L, null, 2L, null);
        var mapper = JsonMapper.builder().findAndAddModules().build();

        assertFalse(mapper.readTree(mapper.writeValueAsString(linea)).has("formaValida"));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(linea).stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("formaValida")));
        }
    }
}
