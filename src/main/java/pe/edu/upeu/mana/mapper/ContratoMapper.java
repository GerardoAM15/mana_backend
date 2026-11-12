package pe.edu.upeu.mana.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.service.EstadoContratoActual;
import pe.edu.upeu.mana.dto.consumo.ConsumoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.ContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.DetalleContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.PagoContratoResponseDTO;
import pe.edu.upeu.mana.dto.contrato.PensionistaResponseDTO;

@Component
public class ContratoMapper {

    public ContratoMapper(EstadoContratoActual estados) {
        this.estados = estados;
    }

    private final EstadoContratoActual estados;

    public DetalleContratoResponseDTO toDetalle(DetalleContrato d) {
        return new DetalleContratoResponseDTO(d.getId(), d.getServicio(), d.getRacionesContratadas(), d.getSaldoRaciones(), d.getPrecioRacionPactado(), d.getSubtotal());
    }

    public ContratoResponseDTO toResponse(Contrato c, Cliente cliente, TipoPlan plan, List<DetalleContrato> detalles, List<PagoContrato> pagos, List<ConsumoResponseDTO> consumos) {
        return new ContratoResponseDTO(c.getId(), c.getNumero(), c.getLocalId(), c.getClienteId(), cliente.getDni(), cliente.getNombre() + " " + cliente.getApellido(), c.getPlanId(), plan.getNombre(), estados.calcular(c), c.getFechaInicio(), c.getFechaFin(), c.getMontoTotal(), c.getAnteriorId(), c.getAnuladoEn(), c.getMotivoAnulacion(), detalles.stream().map(this::toDetalle).toList(), pagos.stream().map(p -> new PagoContratoResponseDTO(p.getId(), p.getCajaId(), p.getTipo(), p.getMetodo(), p.getMonto(), p.getFecha(), p.getReferencia())).toList(), consumos, c.getVersion());
    }

    public PensionistaResponseDTO toPensionista(Contrato c, Cliente p, List<DetalleContrato> detalles) {
        return new PensionistaResponseDTO(p.getId(), p.getDni(), p.getNombre() + " " + p.getApellido(), c.getId(), c.getNumero(), estados.calcular(c), c.getFechaInicio(), c.getFechaFin(), detalles.stream().map(this::toDetalle).toList());
    }
}
