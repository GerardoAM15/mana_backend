package pe.edu.upeu.mana.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.EstadoContrato;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    @Query("select c.clienteId from Contrato c where c.id=:id")
    Optional<Long> buscarClienteId(@Param("id") Long id);

    @Query("select count(c)>0 from Contrato c where c.clienteId=:clienteId and c.estado in (pe.edu.upeu.mana.enums.EstadoContrato.PROGRAMADO,pe.edu.upeu.mana.enums.EstadoContrato.ACTIVO) and c.fechaInicio<=:fin and c.fechaFin>=:inicio")
    boolean existeSuperposicion(@Param("clienteId") Long clienteId, @Param("inicio") java.time.LocalDate inicio, @Param("fin") java.time.LocalDate fin);

    @Query("select c from Contrato c, Cliente p where p.id=c.clienteId and c.localId=:localId and (:dni is null or p.dni=:dni) and (:desde is null or c.fechaFin>=:desde) and (:hasta is null or c.fechaInicio<=:hasta) and (:estado is null or (:estado='ANULADO' and c.estado=pe.edu.upeu.mana.enums.EstadoContrato.ANULADO) or (:estado='FINALIZADO' and (c.estado=pe.edu.upeu.mana.enums.EstadoContrato.FINALIZADO or (c.estado in (pe.edu.upeu.mana.enums.EstadoContrato.ACTIVO,pe.edu.upeu.mana.enums.EstadoContrato.PROGRAMADO) and c.fechaFin<:hoy))) or (:estado='ACTIVO' and c.estado in (pe.edu.upeu.mana.enums.EstadoContrato.ACTIVO,pe.edu.upeu.mana.enums.EstadoContrato.PROGRAMADO) and c.fechaInicio<=:hoy and c.fechaFin>=:hoy) or (:estado='PROGRAMADO' and c.estado in (pe.edu.upeu.mana.enums.EstadoContrato.ACTIVO,pe.edu.upeu.mana.enums.EstadoContrato.PROGRAMADO) and c.fechaInicio>:hoy))")
    org.springframework.data.domain.Page<Contrato> filtrar(@Param("localId") Long localId, @Param("dni") String dni, @Param("desde") java.time.LocalDate desde, @Param("hasta") java.time.LocalDate hasta, org.springframework.data.domain.Pageable pageable, @Param("estado") String estado, @Param("hoy") java.time.LocalDate hoy);

    @Query("select c from Contrato c where c.clienteId=:clienteId and c.localId=:localId and c.fechaInicio<=:hoy and c.fechaFin>=:hoy and c.estado in (pe.edu.upeu.mana.enums.EstadoContrato.PROGRAMADO,pe.edu.upeu.mana.enums.EstadoContrato.ACTIVO)")
    Optional<Contrato> buscarVigente(@Param("clienteId") Long clienteId, @Param("localId") Long localId, @Param("hoy") java.time.LocalDate hoy);

    @Query("select c from Contrato c, Cliente p where p.id=c.clienteId and c.localId=:localId and c.fechaInicio<=:hoy and c.fechaFin>=:hoy and c.estado in (pe.edu.upeu.mana.enums.EstadoContrato.PROGRAMADO,pe.edu.upeu.mana.enums.EstadoContrato.ACTIVO) and (p.dni like :texto escape '!' or lower(concat(concat(p.nombre,' '),p.apellido)) like :texto escape '!') order by p.apellido,p.nombre")
    java.util.List<Contrato> buscarPensionistas(@Param("localId") Long localId, @Param("hoy") java.time.LocalDate hoy, @Param("texto") String texto);

    @Modifying
    @Query("update Contrato c set c.estado=pe.edu.upeu.mana.enums.EstadoContrato.FINALIZADO,c.version=c.version+1,c.actualizadoEn=:ahora,c.actualizadoPor='sistema' where c.estado in (pe.edu.upeu.mana.enums.EstadoContrato.ACTIVO,pe.edu.upeu.mana.enums.EstadoContrato.PROGRAMADO) and c.fechaFin<:hoy")
    int finalizarVencidos(@Param("hoy") java.time.LocalDate hoy, @Param("ahora") java.time.OffsetDateTime ahora);

    @Modifying
    @Query("update Contrato c set c.estado=pe.edu.upeu.mana.enums.EstadoContrato.ACTIVO,c.version=c.version+1,c.actualizadoEn=:ahora,c.actualizadoPor='sistema' where c.estado=pe.edu.upeu.mana.enums.EstadoContrato.PROGRAMADO and c.fechaInicio<=:hoy and c.fechaFin>=:hoy")
    int activarProgramados(@Param("hoy") java.time.LocalDate hoy, @Param("ahora") java.time.OffsetDateTime ahora);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Contrato e where e.id = :id")
    Optional<Contrato> buscarParaActualizar(@Param("id") Long id);
}
