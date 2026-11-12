package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from MetodoPago e where e.codigo = :id")
    Optional<MetodoPago> buscarParaActualizar(@Param("id") String id);
}
