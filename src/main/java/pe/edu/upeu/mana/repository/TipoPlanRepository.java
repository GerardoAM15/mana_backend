package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface TipoPlanRepository extends JpaRepository<TipoPlan, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from TipoPlan e where e.id = :id")
    Optional<TipoPlan> buscarParaActualizar(@Param("id") Long id);
}
