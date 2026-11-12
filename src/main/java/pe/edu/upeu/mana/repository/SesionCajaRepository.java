package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface SesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    Optional<SesionCaja> findByClaveAbierta(String claveAbierta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from SesionCaja c where c.claveAbierta=:clave")
    Optional<SesionCaja> buscarAbiertaParaActualizar(@Param("clave") String clave);

    @Query("select c from SesionCaja c where c.localId=:localId and (:usuarioId is null or c.usuarioId=:usuarioId) and (:desde is null or c.apertura>=:desde) and (:hasta is null or c.apertura<:hasta)")
    org.springframework.data.domain.Page<SesionCaja> filtrar(@Param("localId") Long localId, @Param("usuarioId") Long usuarioId, @Param("desde") java.time.LocalDateTime desde, @Param("hasta") java.time.LocalDateTime hasta, org.springframework.data.domain.Pageable pageable);

    interface MovimientoCaja {

        String getMetodo();

        java.math.BigDecimal getMonto();

        Long getMovimientos();
    }

    @Query(value = """
        select metodo as metodo, sum(monto) as monto, count(*) as movimientos
        from (
            select p.metodo, p.monto
              from pagos_venta p join ventas v on v.id=p.venta_id where v.caja_id=:cajaId
            union all
            select d.metodo, -d.monto
              from devoluciones_venta d join ventas v on v.id=d.venta_id where v.caja_id=:cajaId
            union all
            select p.metodo, case when p.tipo='COBRO' then p.monto else -p.monto end
              from pagos_contrato p where p.caja_id=:cajaId
            union all
            select c.metodo_recargo, c.recargo_llevar
              from consumos c where c.caja_id=:cajaId and c.anulado=0 and c.recargo_llevar>0
        ) group by metodo order by metodo
        """, nativeQuery = true)
    java.util.List<MovimientoCaja> sumarMovimientos(@Param("cajaId") Long cajaId);

    interface ConteoCaja {

        Long getVentas();

        Long getConsumos();

        Long getContratos();
    }

    @Query(value = """
        select (select count(*) from ventas where caja_id=:cajaId) as ventas,
               (select count(*) from consumos where caja_id=:cajaId and anulado=0) as consumos,
               (select count(distinct contrato_id) from pagos_contrato where caja_id=:cajaId) as contratos
          from dual
        """, nativeQuery = true)
    ConteoCaja contarOperaciones(@Param("cajaId") Long cajaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from SesionCaja e where e.id = :id")
    Optional<SesionCaja> buscarParaActualizar(@Param("id") Long id);
}
