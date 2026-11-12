package pe.edu.upeu.mana.repository;

import pe.edu.upeu.mana.enums.TipoCorrelativo;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface CorrelativoRepository extends JpaRepository<Correlativo, CorrelativoId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Correlativo e where e.localId = :localId and e.tipo = :tipo")
    Optional<Correlativo> buscarParaActualizar(@Param("localId") Long localId, @Param("tipo") TipoCorrelativo tipo);
}
