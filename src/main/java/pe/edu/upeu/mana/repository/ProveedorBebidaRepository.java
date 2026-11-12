package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface ProveedorBebidaRepository extends JpaRepository<ProveedorBebida, ProveedorBebidaId> {

    java.util.List<ProveedorBebida> findByProveedorId(Long proveedorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ProveedorBebida e where e.bebidaId = :bebidaId and e.proveedorId = :proveedorId")
    Optional<ProveedorBebida> buscarParaActualizar(@Param("bebidaId") Long bebidaId, @Param("proveedorId") Long proveedorId);
}
