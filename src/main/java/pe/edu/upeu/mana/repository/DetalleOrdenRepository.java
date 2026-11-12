package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    java.util.List<DetalleOrden> findByVentaIdOrderById(Long ventaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DetalleOrden e where e.id = :id")
    Optional<DetalleOrden> buscarParaActualizar(@Param("id") Long id);
}
