package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.dto.pos.BebidaPosResponseDTO;

public interface BebidaRepository extends JpaRepository<Bebida, Long> {

    @Query("select new pe.edu.upeu.mana.dto.pos.BebidaPosResponseDTO(b.id,b.nombre,b.precioVenta,coalesce(s.stock,0L)) from Bebida b left join StockBebida s on s.bebidaId=b.id and s.localId=:localId where b.activo=true order by b.nombre")
    java.util.List<pe.edu.upeu.mana.dto.pos.BebidaPosResponseDTO> listarPos(@Param("localId") Long localId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Bebida e where e.id = :id")
    Optional<Bebida> buscarParaActualizar(@Param("id") Long id);
}
