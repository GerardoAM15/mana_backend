package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Cliente e where e.id = :id")
    Optional<Cliente> buscarParaActualizar(@Param("id") Long id);
}
