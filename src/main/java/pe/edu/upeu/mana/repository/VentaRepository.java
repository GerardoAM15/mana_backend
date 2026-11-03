package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.EstadoVenta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("select v.cajaId from Venta v where v.id=:id")
    Optional<Long> buscarCajaId(@Param("id") Long id);

    @Query("select v from Venta v where v.localId=:localId and (:desde is null or v.fecha>=:desde) and (:hasta is null or v.fecha<:hasta) and (:estado is null or v.estado=:estado) and (:cajaId is null or v.cajaId=:cajaId)")
    org.springframework.data.domain.Page<Venta> filtrar(@Param("localId") Long localId, @Param("desde") java.time.LocalDateTime desde, @Param("hasta") java.time.LocalDateTime hasta, @Param("estado") pe.edu.upeu.mana.enums.EstadoVenta estado, @Param("cajaId") Long cajaId, org.springframework.data.domain.Pageable pageable);

    boolean existsByCajaIdAndEstado(Long cajaId, pe.edu.upeu.mana.enums.EstadoVenta estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Venta e where e.id = :id")
    Optional<Venta> buscarParaActualizar(@Param("id") Long id);
}
