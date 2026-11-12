package pe.edu.upeu.mana.repository;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import pe.edu.upeu.mana.entity.Comprobante;
import pe.edu.upeu.mana.enums.*;
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long>, JpaSpecificationExecutor<Comprobante> {
    Optional<Comprobante> findByVentaIdAndEstadoNot(Long ventaId, EstadoComprobante estado);
    Optional<Comprobante> findByContratoIdAndEstadoNot(Long contratoId, EstadoComprobante estado);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comprobante c where c.id = :id")
    Optional<Comprobante> bloquear(Long id);
    @Query("select c.id from Comprobante c where c.estado in :estados and c.intentos < 5 order by c.id")
    List<Long> reintentables(Collection<EstadoComprobante> estados, Pageable pagina);
    @Query("select c.id from Comprobante c where c.estado in :estados and c.intentos < 5 and c.id > :ultimo order by c.id")
    List<Long> reintentablesDespues(Collection<EstadoComprobante> estados, Long ultimo, Pageable pagina);
}

