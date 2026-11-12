package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tokens_restablecimiento")
public class TokenRestablecimiento extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "usuario_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long usuarioId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @org.hibernate.annotations.TimeZoneStorage(org.hibernate.annotations.TimeZoneStorageType.NATIVE)
    @Column(name = "expira_en", nullable = false, columnDefinition = "timestamp(9) with time zone")
    private OffsetDateTime expiraEn;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "usado", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean usado = false;
}
