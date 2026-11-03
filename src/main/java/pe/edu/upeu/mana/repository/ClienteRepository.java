package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    @Query("select c from Cliente c where c.dni=:dni or lower(c.nombre) like :texto escape '!' or lower(c.apellido) like :texto escape '!'")
    org.springframework.data.domain.Page<Cliente> filtrar(@Param("dni") String dni, @Param("texto") String texto, org.springframework.data.domain.Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Cliente e where e.id = :id")
    Optional<Cliente> buscarParaActualizar(@Param("id") Long id);
}
