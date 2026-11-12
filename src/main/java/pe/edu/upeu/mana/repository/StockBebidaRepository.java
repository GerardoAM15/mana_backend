package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.inventario.StockBebidaResponseDTO;

public interface StockBebidaRepository extends JpaRepository<StockBebida, Long> {

    java.util.List<StockBebida> findByLocalIdAndBebidaIdInOrderById(Long localId, java.util.Collection<Long> bebidaIds);

    @Query("select new pe.edu.upeu.mana.dto.inventario.StockBebidaResponseDTO(s.id,s.localId,b.id,b.nombre,s.stock) from StockBebida s,Bebida b where b.id=s.bebidaId and s.localId=:localId order by b.nombre")
    java.util.List<pe.edu.upeu.mana.dto.inventario.StockBebidaResponseDTO> listarStock(@Param("localId") Long localId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from StockBebida e where e.id = :id")
    Optional<StockBebida> buscarParaActualizar(@Param("id") Long id);
}
