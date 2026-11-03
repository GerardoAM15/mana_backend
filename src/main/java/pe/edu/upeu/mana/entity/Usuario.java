package pe.edu.upeu.mana.entity;

import pe.edu.upeu.mana.enums.RolUsuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "empleado_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long empleadoId;

    @Column(name = "correo", nullable = false, length = 160)
    private String correo;

    @Column(name = "clave_hash", nullable = false, length = 100)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 32, columnDefinition = "varchar2(32 char)")
    private RolUsuario rol;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "activo", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean activo = true;

    @Column(name = "token_version", nullable = false, precision = 10, scale = 0, columnDefinition = "number(10,0)")
    private Long tokenVersion = 0L;
}
