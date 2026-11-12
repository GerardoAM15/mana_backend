package pe.edu.upeu.mana.repository;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import pe.edu.upeu.mana.entity.SerieComprobante;
import pe.edu.upeu.mana.enums.TipoSerie;
public interface SerieComprobanteRepository extends JpaRepository<SerieComprobante, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SerieComprobante s where s.localId = :localId and s.tipo = :tipo and s.activo = true order by s.id")
    java.util.List<SerieComprobante> bloquearActivas(Long localId, TipoSerie tipo);
}
