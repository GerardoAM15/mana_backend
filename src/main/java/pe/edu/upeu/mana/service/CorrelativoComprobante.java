package pe.edu.upeu.mana.service;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.*;
import pe.edu.upeu.mana.repository.SerieComprobanteRepository;
import pe.edu.upeu.mana.enums.TipoSerie;
import pe.edu.upeu.mana.exception.ReglaNegocioException;
@Component
public class CorrelativoComprobante {
    public record Numero(Long serieId, String serie, int numero) {
        public String formateado() { return "%s-%08d".formatted(serie, numero); }
    }
    private final SerieComprobanteRepository series;
    public CorrelativoComprobante(SerieComprobanteRepository series) { this.series=series; }
    @Transactional(propagation = Propagation.MANDATORY)
    public Numero siguiente(Long local, TipoSerie tipo) {
        var activas = series.bloquearActivas(local, tipo);
        if (activas.size() != 1) throw new ReglaNegocioException("Debe existir exactamente una serie activa para el local y tipo de comprobante");
        var s = activas.getFirst();
        if (s.getUltimoNumero() >= 99999999) throw new ReglaNegocioException("La serie de comprobantes agotó sus números");
        s.setUltimoNumero(s.getUltimoNumero() + 1);
        series.saveAndFlush(s);
        return new Numero(s.getId(), s.getSerie(), s.getUltimoNumero());
    }
}
