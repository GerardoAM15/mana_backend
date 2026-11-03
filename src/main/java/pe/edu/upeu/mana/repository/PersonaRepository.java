package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    boolean existsByDniAndIdNot(String dni, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Persona e where e.id = :id")
    Optional<Persona> buscarParaActualizar(@Param("id") Long id);
}
