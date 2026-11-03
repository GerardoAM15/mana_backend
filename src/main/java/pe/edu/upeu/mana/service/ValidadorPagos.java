package pe.edu.upeu.mana.service;

import java.math.*;
import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.dto.comun.PagoRequestDTO;
import pe.edu.upeu.mana.repository.MetodoPagoRepository;
import pe.edu.upeu.mana.exception.*;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ValidadorPagos {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidadorPagos.class);

    private final MetodoPagoRepository metodos;

    public ValidadorPagos(MetodoPagoRepository metodos) {
        this.metodos = metodos;
    }

    @Transactional(readOnly = true)
    public void validar(List<PagoRequestDTO> pagos, BigDecimal total) {
        long inicioOperacion = System.nanoTime();
        log.info("Inicio ValidadorPagos.validar");
        try {
            if (pagos == null || pagos.isEmpty())
                throw new DatosInvalidosException("Los pagos son obligatorios");
            BigDecimal suma = new BigDecimal("0.00");
            for (var p : pagos) {
                if (p == null || p.getMonto() == null || p.getMonto().signum() <= 0 || p.getMonto().stripTrailingZeros().scale() > 2)
                    throw new DatosInvalidosException("Los pagos deben ser positivos y tener hasta dos decimales");
                if (p.getMetodo() == null || !metodos.findById(p.getMetodo()).map(m -> Boolean.TRUE.equals(m.getActivo())).orElse(false))
                    throw new ReglaNegocioException("Método de pago inexistente o inactivo");
                suma = suma.add(p.getMonto());
            }
            if (suma.compareTo(total) != 0)
                throw new ReglaNegocioException("La suma de los pagos debe ser igual al total");
        } finally {
            log.info("Fin ValidadorPagos.validar | duracionMs={}", (System.nanoTime() - inicioOperacion) / 1_000_000);
        }
    }
}
