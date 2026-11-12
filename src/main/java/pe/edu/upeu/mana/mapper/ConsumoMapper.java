package pe.edu.upeu.mana.mapper;

import java.util.*;
import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.consumo.ConsumoResponseDTO;
import pe.edu.upeu.mana.dto.consumo.DetalleConsumoResponseDTO;

@Component
public class ConsumoMapper {

    public ConsumoResponseDTO toResponse(Consumo c, DetalleContrato d, List<DetalleConsumo> detalles, Map<Long, Plato> platos) {
        return new ConsumoResponseDTO(c.getId(), d.getContratoId(), d.getId(), d.getServicio(), c.getFecha(), c.getRegistradoEn(), c.getValorRacion(), c.getParaLlevar(), c.getRecargoLlevar(), c.getMetodoRecargo(), c.getCajaId(), c.getAnulado(), c.getAnuladoEn(), c.getMotivoAnulacion(), detalles.stream().map(p -> new DetalleConsumoResponseDTO(p.getPlatoId(), platos.get(p.getPlatoId()).getNombre(), p.getCategoriaConsumida(), p.getCantidad())).toList());
    }
}
