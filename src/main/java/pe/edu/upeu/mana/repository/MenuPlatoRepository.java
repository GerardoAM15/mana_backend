package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface MenuPlatoRepository extends JpaRepository<MenuPlato, MenuPlatoId> {

    java.util.List<MenuPlato> findByMenuId(Long menuId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from MenuPlato e where e.menuId = :menuId and e.platoId = :platoId")
    Optional<MenuPlato> buscarParaActualizar(@Param("menuId") Long menuId, @Param("platoId") Long platoId);
}
