package pe.edu.upeu.mana.repository;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.Contrato;
import pe.edu.upeu.mana.dto.reporte.AsistenciaDiariaResponseDTO;
import pe.edu.upeu.mana.dto.reporte.ContratoPorVencerResponseDTO;
import pe.edu.upeu.mana.dto.reporte.PlatoConsumidoResponseDTO;
import pe.edu.upeu.mana.enums.EstadoContrato;

public interface ReporteRepository extends Repository<Contrato, Long> {

    interface IngresoPeriodo {

        String getPeriodo();

        BigDecimal getContratos();

        BigDecimal getVentas();
    }

    @Query(value = """
        select to_char(fecha,'YYYY-MM') periodo,sum(contratos) contratos,sum(ventas) ventas from (
        select c.local_id,p.fecha,case when p.tipo='COBRO' then p.monto else -p.monto end contratos,0 ventas,0 gastos
        from pagos_contrato p join contratos c on c.id=p.contrato_id
        union all select v.local_id,p.fecha,0,p.monto,0 from pagos_venta p join ventas v on v.id=p.venta_id where v.estado in ('PAGADA','ANULADA')
        union all select v.local_id,d.fecha,0,-d.monto,0 from devoluciones_venta d join ventas v on v.id=d.venta_id
        ) where local_id=:localId and fecha>=:desde and fecha<:hasta
        group by to_char(fecha,'YYYY-MM') order by periodo
        """, nativeQuery = true)
    List<IngresoPeriodo> ingresos(@Param("localId") Long localId, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("select new pe.edu.upeu.mana.dto.reporte.PlatoConsumidoResponseDTO(p.id,p.nombre,d.servicio,sum(dc.cantidad)) from DetalleConsumo dc,Consumo c,DetalleContrato d,Contrato co,Plato p where dc.consumoId=c.id and c.detalleContratoId=d.id and d.contratoId=co.id and p.id=dc.platoId and co.localId=:localId and c.anulado=false and c.fecha>=:desde and c.fecha<=:hasta and (:servicio is null or d.servicio=:servicio) group by p.id,p.nombre,d.servicio order by sum(dc.cantidad) desc,p.nombre")
    List<PlatoConsumidoResponseDTO> platos(@Param("localId") Long localId, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("servicio") String servicio);

    @Query("select new pe.edu.upeu.mana.dto.reporte.ContratoPorVencerResponseDTO(c.id,c.numero,p.dni,concat(concat(p.nombre,' '),p.apellido),c.fechaFin,d.servicio,d.saldoRaciones) from Contrato c,Cliente p,DetalleContrato d where c.clienteId=p.id and d.contratoId=c.id and c.localId=:localId and c.estado in (pe.edu.upeu.mana.enums.EstadoContrato.ACTIVO,pe.edu.upeu.mana.enums.EstadoContrato.PROGRAMADO) and c.fechaInicio<=:hoy and c.fechaFin>=:desde and c.fechaFin<=:hasta order by c.fechaFin,c.id,d.servicio")
    List<ContratoPorVencerResponseDTO> porVencer(@Param("localId") Long localId, @Param("hoy") LocalDate hoy, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("select new pe.edu.upeu.mana.dto.reporte.AsistenciaDiariaResponseDTO(c.fecha,d.servicio,count(c.id)) from Consumo c,DetalleContrato d,Contrato co where c.detalleContratoId=d.id and d.contratoId=co.id and co.localId=:localId and c.anulado=false and c.fecha>=:desde and c.fecha<=:hasta group by c.fecha,d.servicio order by c.fecha,d.servicio")
    List<AsistenciaDiariaResponseDTO> asistencia(@Param("localId") Long localId, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    interface GastoIngreso {

        String getPeriodo();

        BigDecimal getIngresos();

        BigDecimal getGastos();
    }

    @Query(value = """
        select periodo,sum(contratos+ventas) ingresos,sum(gastos) gastos from (
        select to_char(fecha,:formato) periodo,contratos,ventas,gastos from (
        select c.local_id,p.fecha,case when p.tipo='COBRO' then p.monto else -p.monto end contratos,0 ventas,0 gastos
        from pagos_contrato p join contratos c on c.id=p.contrato_id
        union all select v.local_id,p.fecha,0,p.monto,0 from pagos_venta p join ventas v on v.id=p.venta_id where v.estado in ('PAGADA','ANULADA')
        union all select v.local_id,d.fecha,0,-d.monto,0 from devoluciones_venta d join ventas v on v.id=d.venta_id
        union all select local_id,fecha,0,0,monto from gastos_diarios where activo=1
        ) where local_id=:localId and fecha>=:desde and fecha<:hasta
        ) group by periodo order by periodo
        """, nativeQuery = true)
    List<GastoIngreso> gastos(@Param("localId") Long localId, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta, @Param("formato") String formato);
}
