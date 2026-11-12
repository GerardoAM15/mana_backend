package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Proveedor e where e.id = :id")
    Optional<Proveedor> buscarParaActualizar(@Param("id") Long id);
}
