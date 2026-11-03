package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface GastoDiarioRepository extends JpaRepository<GastoDiario, Long> {

    @Query("select g from GastoDiario g where g.localId=:localId and (:desde is null or g.fecha>=:desde) and (:hasta is null or g.fecha<=:hasta)")
    org.springframework.data.domain.Page<GastoDiario> filtrar(@Param("localId") Long localId, @Param("desde") java.time.LocalDate desde, @Param("hasta") java.time.LocalDate hasta, org.springframework.data.domain.Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from GastoDiario e where e.id = :id")
    Optional<GastoDiario> buscarParaActualizar(@Param("id") Long id);
}
