package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface ServicioDiaAtencionRepository extends JpaRepository<ServicioDiaAtencion, ServicioDiaAtencionId> {

    java.util.List<ServicioDiaAtencion> findByServicio(String servicio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ServicioDiaAtencion e where e.servicio = :servicio and e.diaSemana = :diaSemana")
    Optional<ServicioDiaAtencion> buscarParaActualizar(@Param("servicio") String servicio, @Param("diaSemana") Integer diaSemana);
}
