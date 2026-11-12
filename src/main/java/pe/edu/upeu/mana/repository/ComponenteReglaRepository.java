package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface ComponenteReglaRepository extends JpaRepository<ComponenteRegla, ComponenteReglaId> {

    java.util.List<ComponenteRegla> findByReglaId(Long reglaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ComponenteRegla e where e.reglaId = :reglaId and e.categoria = :categoria")
    Optional<ComponenteRegla> buscarParaActualizar(@Param("reglaId") Long reglaId, @Param("categoria") String categoria);
}
