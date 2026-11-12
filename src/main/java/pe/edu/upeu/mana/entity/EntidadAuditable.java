package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadAuditable {

    @jakarta.persistence.Version
    @Column(name = "version", nullable = false, precision = 19, scale = 0)
    private Long version;

    @CreatedDate
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    @Column(name = "creado_en", nullable = false, updatable = false, columnDefinition = "timestamp(9) with time zone")
    private OffsetDateTime creadoEn;

    @LastModifiedDate
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    @Column(name = "actualizado_en", nullable = false, columnDefinition = "timestamp(9) with time zone")
    private OffsetDateTime actualizadoEn;

    @CreatedBy
    @Column(name = "creado_por", nullable = false, updatable = false, length = 160)
    private String creadoPor;

    @LastModifiedBy
    @Column(name = "actualizado_por", nullable = false, length = 160)
    private String actualizadoPor;
}
