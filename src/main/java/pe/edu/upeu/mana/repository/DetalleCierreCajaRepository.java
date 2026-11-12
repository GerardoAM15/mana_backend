package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface DetalleCierreCajaRepository extends JpaRepository<DetalleCierreCaja, Long> {

    java.util.List<DetalleCierreCaja> findBySesionCajaIdOrderByMetodo(Long sesionCajaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DetalleCierreCaja e where e.id = :id")
    Optional<DetalleCierreCaja> buscarParaActualizar(@Param("id") Long id);
}
