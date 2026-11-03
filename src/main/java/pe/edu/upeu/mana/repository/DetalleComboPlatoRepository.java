package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface DetalleComboPlatoRepository extends JpaRepository<DetalleComboPlato, DetalleComboPlatoId> {

    java.util.List<DetalleComboPlato> findByDetalleIdOrderByPlatoId(Long detalleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DetalleComboPlato e where e.detalleId = :detalleId and e.platoId = :platoId")
    Optional<DetalleComboPlato> buscarParaActualizar(@Param("detalleId") Long detalleId, @Param("platoId") Long platoId);
}
