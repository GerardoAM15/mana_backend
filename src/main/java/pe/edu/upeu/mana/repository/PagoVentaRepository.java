package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface PagoVentaRepository extends JpaRepository<PagoVenta, Long> {

    java.util.List<PagoVenta> findByVentaIdOrderById(Long ventaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PagoVenta e where e.id = :id")
    Optional<PagoVenta> buscarParaActualizar(@Param("id") Long id);
}
