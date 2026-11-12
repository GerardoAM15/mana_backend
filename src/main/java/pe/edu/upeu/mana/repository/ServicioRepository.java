package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface ServicioRepository extends JpaRepository<Servicio, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Servicio e where e.codigo = :id")
    Optional<Servicio> buscarParaActualizar(@Param("id") String id);
}
