package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;

public interface TokenRestablecimientoRepository extends JpaRepository<TokenRestablecimiento, Long> {

    @Query("select t.usuarioId from TokenRestablecimiento t where t.tokenHash = :hash")
    Optional<Long> buscarUsuarioIdPorHash(@Param("hash") String hash);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update TokenRestablecimiento t set t.usado = true where t.usuarioId = :usuarioId and t.usado = false")
    int invalidarPendientes(@Param("usuarioId") Long usuarioId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TokenRestablecimiento t where t.tokenHash = :hash")
    Optional<TokenRestablecimiento> buscarPorHashParaActualizar(@Param("hash") String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from TokenRestablecimiento e where e.id = :id")
    Optional<TokenRestablecimiento> buscarParaActualizar(@Param("id") Long id);
}
