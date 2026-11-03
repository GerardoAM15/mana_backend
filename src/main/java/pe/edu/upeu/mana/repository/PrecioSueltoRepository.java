package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface PrecioSueltoRepository extends JpaRepository<PrecioSuelto, PrecioSueltoId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PrecioSuelto e where e.servicio = :servicio and e.categoria = :categoria")
    Optional<PrecioSuelto> buscarParaActualizar(@Param("servicio") String servicio, @Param("categoria") String categoria);
}
