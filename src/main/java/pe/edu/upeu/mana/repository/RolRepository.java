package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface RolRepository extends JpaRepository<Rol, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Rol e where e.codigo = :id")
    Optional<Rol> buscarParaActualizar(@Param("id") String id);
}
