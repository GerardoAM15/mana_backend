package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface PlanServicioRepository extends JpaRepository<PlanServicio, PlanServicioId> {

    java.util.List<PlanServicio> findByPlanId(Long planId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PlanServicio e where e.planId = :planId and e.servicio = :servicio")
    Optional<PlanServicio> buscarParaActualizar(@Param("planId") Long planId, @Param("servicio") String servicio);
}
