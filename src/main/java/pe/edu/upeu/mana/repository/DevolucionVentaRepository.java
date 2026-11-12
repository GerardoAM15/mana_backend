package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface DevolucionVentaRepository extends JpaRepository<DevolucionVenta, Long> {

    java.util.List<DevolucionVenta> findByVentaIdOrderById(Long ventaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DevolucionVenta e where e.id = :id")
    Optional<DevolucionVenta> buscarParaActualizar(@Param("id") Long id);
}
