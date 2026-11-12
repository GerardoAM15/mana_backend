package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface PagoContratoRepository extends JpaRepository<PagoContrato, Long> {

    java.util.List<PagoContrato> findByContratoIdOrderById(Long contratoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PagoContrato e where e.id = :id")
    Optional<PagoContrato> buscarParaActualizar(@Param("id") Long id);
}
