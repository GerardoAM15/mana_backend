package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface CompraBebidaRepository extends JpaRepository<CompraBebida, Long> {

    @Query("select c from CompraBebida c where c.localId=:localId and (:desde is null or c.fecha>=:desde) and (:hasta is null or c.fecha<=:hasta) order by c.fecha desc,c.id desc")
    java.util.List<CompraBebida> filtrar(@Param("localId") Long localId, @Param("desde") java.time.LocalDate desde, @Param("hasta") java.time.LocalDate hasta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CompraBebida e where e.id = :id")
    Optional<CompraBebida> buscarParaActualizar(@Param("id") Long id);
}
