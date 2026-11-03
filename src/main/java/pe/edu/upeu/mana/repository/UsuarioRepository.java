package pe.edu.upeu.mana.repository;

import pe.edu.upeu.mana.enums.RolUsuario;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByRol(RolUsuario rol);

    long countByRolAndActivoTrue(RolUsuario rol);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Usuario e where e.id = :id")
    Optional<Usuario> buscarParaActualizar(@Param("id") Long id);
}
