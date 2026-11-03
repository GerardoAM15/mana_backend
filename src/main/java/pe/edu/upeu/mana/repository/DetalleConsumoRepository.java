package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface DetalleConsumoRepository extends JpaRepository<DetalleConsumo, Long> {

    java.util.List<DetalleConsumo> findByConsumoIdOrderById(Long consumoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DetalleConsumo e where e.id = :id")
    Optional<DetalleConsumo> buscarParaActualizar(@Param("id") Long id);
}
