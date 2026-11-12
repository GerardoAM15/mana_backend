package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface CategoriaPlatoRepository extends JpaRepository<CategoriaPlato, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CategoriaPlato e where e.codigo = :id")
    Optional<CategoriaPlato> buscarParaActualizar(@Param("id") String id);
}
