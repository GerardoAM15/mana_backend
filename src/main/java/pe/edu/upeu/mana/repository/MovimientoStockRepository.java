package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.inventario.MovimientoStockResponseDTO;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    @Query("select new pe.edu.upeu.mana.dto.inventario.MovimientoStockResponseDTO(m.id,m.localId,b.id,b.nombre,m.tipo,m.cantidad,m.fecha,m.detalleCompraId,m.detalleOrdenId,m.motivo) from MovimientoStock m,Bebida b where b.id=m.bebidaId and m.localId=:localId and (:bebidaId is null or m.bebidaId=:bebidaId) and (:desde is null or m.fecha>=:desde) and (:hasta is null or m.fecha<:hasta)")
    org.springframework.data.domain.Page<pe.edu.upeu.mana.dto.inventario.MovimientoStockResponseDTO> filtrar(@Param("localId") Long localId, @Param("bebidaId") Long bebidaId, @Param("desde") java.time.LocalDateTime desde, @Param("hasta") java.time.LocalDateTime hasta, org.springframework.data.domain.Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from MovimientoStock e where e.id = :id")
    Optional<MovimientoStock> buscarParaActualizar(@Param("id") Long id);
}
