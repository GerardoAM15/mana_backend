package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface DetalleContratoRepository extends JpaRepository<DetalleContrato, Long> {

    java.util.List<DetalleContrato> findByContratoIdOrderById(Long contratoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DetalleContrato d where d.contratoId=:contratoId and d.servicio=:servicio")
    Optional<DetalleContrato> buscarServicioParaActualizar(@Param("contratoId") Long contratoId, @Param("servicio") String servicio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DetalleContrato e where e.id = :id")
    Optional<DetalleContrato> buscarParaActualizar(@Param("id") Long id);
}
