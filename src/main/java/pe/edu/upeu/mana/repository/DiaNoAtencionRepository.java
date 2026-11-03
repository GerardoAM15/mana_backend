package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface DiaNoAtencionRepository extends JpaRepository<DiaNoAtencion, Long> {

    boolean existsByLocalIdAndFecha(Long localId, java.time.LocalDate fecha);

    @Query("select d from DiaNoAtencion d where d.localId = :localId and (:desde is null or d.fecha >= :desde) and (:hasta is null or d.fecha <= :hasta) order by d.fecha")
    java.util.List<DiaNoAtencion> filtrar(@Param("localId") Long localId, @Param("desde") java.time.LocalDate desde, @Param("hasta") java.time.LocalDate hasta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DiaNoAtencion e where e.id = :id")
    Optional<DiaNoAtencion> buscarParaActualizar(@Param("id") Long id);
}
