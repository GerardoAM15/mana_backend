package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface ReglaCobroRepository extends JpaRepository<ReglaCobro, Long> {

    java.util.List<ReglaCobro> findByServicioAndActivoTrueOrderById(String servicio);

    boolean existsByServicioAndActivoTrueAndIdNot(String servicio, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ReglaCobro e where e.id = :id")
    Optional<ReglaCobro> buscarParaActualizar(@Param("id") Long id);
}
