package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    java.util.List<DetalleCompra> findByCompraIdOrderById(Long compraId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DetalleCompra e where e.id = :id")
    Optional<DetalleCompra> buscarParaActualizar(@Param("id") Long id);
}
