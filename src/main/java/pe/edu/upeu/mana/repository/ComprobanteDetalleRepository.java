package pe.edu.upeu.mana.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.mana.entity.ComprobanteDetalle;
public interface ComprobanteDetalleRepository extends JpaRepository<ComprobanteDetalle, Long> {
    List<ComprobanteDetalle> findByComprobanteIdOrderByItem(Long comprobanteId);
}
