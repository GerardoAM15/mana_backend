package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

    @Query("select c from Consumo c,DetalleContrato d,Contrato co where c.detalleContratoId=d.id and d.contratoId=co.id and co.localId=:localId and (:contratoId is null or co.id=:contratoId) and (:servicio is null or d.servicio=:servicio) and (:desde is null or c.fecha>=:desde) and (:hasta is null or c.fecha<=:hasta)")
    org.springframework.data.domain.Page<Consumo> filtrar(@Param("localId") Long localId, @Param("contratoId") Long contratoId, @Param("servicio") String servicio, @Param("desde") java.time.LocalDate desde, @Param("hasta") java.time.LocalDate hasta, org.springframework.data.domain.Pageable pageable);

    boolean existsByClaveDiaria(String claveDiaria);

    @Query("select count(c)>0 from Consumo c,DetalleContrato d where c.detalleContratoId=d.id and d.contratoId=:contratoId")
    boolean existePorContrato(@Param("contratoId") Long contratoId);

    @Query("select c from Consumo c,DetalleContrato d where c.detalleContratoId=d.id and d.contratoId=:contratoId order by c.id")
    java.util.List<Consumo> buscarPorContrato(@Param("contratoId") Long contratoId);

    interface UbicacionConsumo {

        Long getDetalleId();

        Long getCajaId();

        Long getContratoId();
    }

    @Query("select c.detalleContratoId as detalleId,c.cajaId as cajaId,d.contratoId as contratoId from Consumo c,DetalleContrato d where c.detalleContratoId=d.id and c.id=:id")
    Optional<UbicacionConsumo> buscarUbicacion(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Consumo e where e.id = :id")
    Optional<Consumo> buscarParaActualizar(@Param("id") Long id);
}
