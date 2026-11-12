package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface TarifaServicioRepository extends JpaRepository<TarifaServicio, Long> {

    Optional<TarifaServicio> findByServicioAndActivoTrue(String servicio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from TarifaServicio e where e.id = :id")
    Optional<TarifaServicio> buscarParaActualizar(@Param("id") Long id);
}
