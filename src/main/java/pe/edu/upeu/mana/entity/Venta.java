package pe.edu.upeu.mana.entity;

import pe.edu.upeu.mana.enums.EstadoVenta;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ventas")
public class Venta extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "numero", nullable = false, length = 12)
    private String numero;

    @Column(name = "local_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long localId;

    @Column(name = "caja_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long cajaId;

    @Column(name = "cliente_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long clienteId;

    @Column(name = "servicio", nullable = false, length = 32)
    private String servicio;

    @Convert(converter = BooleanNumeroConverter.class)
    @Column(name = "para_llevar", nullable = false, precision = 1, scale = 0, columnDefinition = "number(1,0)")
    private Boolean paraLlevar = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 16, columnDefinition = "varchar2(16 char)")
    private EstadoVenta estado;

    @Column(name = "fecha", nullable = false, columnDefinition = "timestamp(9)")
    private LocalDateTime fecha;

    @Column(name = "recargo_llevar", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal recargoLlevar;

    @Column(name = "total", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal total;

    @Column(name = "anulada_en", nullable = true, columnDefinition = "timestamp(9)")
    private LocalDateTime anuladaEn;

    @Column(name = "motivo_anulacion", nullable = true, length = 250)
    private String motivoAnulacion;
}
