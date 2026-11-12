package pe.edu.upeu.mana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.mana.enums.*;

@Getter
@Setter
@Entity
@Table(name = "comprobantes")
public class Comprobante extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long id;

    @Column(name = "local_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long localId;

    @Column(name = "serie_id", nullable = false, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long serieId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 16, columnDefinition = "varchar2(16 char)")
    private TipoComprobante tipo;

    @Column(name = "serie", nullable = false, length = 4, columnDefinition = "varchar2(4 char)")
    private String serie;

    @Column(name = "numero", nullable = false, precision = 8, scale = 0, columnDefinition = "number(8,0)")
    private Integer numero;

    @Column(name = "venta_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long ventaId;

    @Column(name = "contrato_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long contratoId;

    @Column(name = "referencia_id", nullable = true, precision = 19, scale = 0, columnDefinition = "number(19,0)")
    private Long referenciaId;

    @Column(name = "motivo_codigo", nullable = true, length = 2, columnDefinition = "varchar2(2 char)")
    private String motivoCodigo;

    @Column(name = "motivo_descripcion", nullable = true, length = 250, columnDefinition = "varchar2(250 char)")
    private String motivoDescripcion;

    @Column(name = "fecha_emision", nullable = false, columnDefinition = "timestamp(9)")
    private LocalDateTime fechaEmision;

    @Column(name = "moneda", nullable = false, length = 3, columnDefinition = "varchar2(3 char)")
    private String moneda = "PEN";

    @Column(name = "receptor_tipo_doc", nullable = false, length = 1, columnDefinition = "varchar2(1 char)")
    private String receptorTipoDoc;

    @Column(name = "receptor_num_doc", nullable = true, length = 15, columnDefinition = "varchar2(15 char)")
    private String receptorNumDoc;

    @Column(name = "receptor_nombre", nullable = false, length = 160, columnDefinition = "varchar2(160 char)")
    private String receptorNombre;

    @Column(name = "receptor_direccion", nullable = true, length = 250, columnDefinition = "varchar2(250 char)")
    private String receptorDireccion;

    @Column(name = "porcentaje_igv", nullable = false, precision = 5, scale = 2, columnDefinition = "number(5,2)")
    private BigDecimal porcentajeIgv;

    @Column(name = "op_gravadas", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal opGravadas;

    @Column(name = "igv", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal igv;

    @Column(name = "total", nullable = false, precision = 14, scale = 2, columnDefinition = "number(14,2)")
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 16, columnDefinition = "varchar2(16 char)")
    private EstadoComprobante estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "ambiente", nullable = false, length = 12, columnDefinition = "varchar2(12 char)")
    private AmbienteFacturacion ambiente;

    @Column(name = "sunat_codigo", nullable = true, length = 10, columnDefinition = "varchar2(10 char)")
    private String sunatCodigo;

    @Column(name = "sunat_mensaje", nullable = true, length = 500, columnDefinition = "varchar2(500 char)")
    private String sunatMensaje;

    @Column(name = "hash", nullable = true, length = 100, columnDefinition = "varchar2(100 char)")
    private String hash;

    @Lob
    @Column(name = "xml", nullable = true)
    private String xml;

    @Lob
    @Column(name = "cdr", nullable = true)
    private String cdr;

    @Column(name = "intentos", nullable = false, precision = 5, scale = 0, columnDefinition = "number(5,0)")
    private Integer intentos = 0;

    @Column(name = "ultimo_intento_en", nullable = true, columnDefinition = "timestamp(9)")
    private LocalDateTime ultimoIntentoEn;

    @Column(name = "receptor_email", nullable = true, length = 120, columnDefinition = "varchar2(120 char)")
    private String receptorEmail;

    @Column(name = "correo_enviado_en", nullable = true, columnDefinition = "timestamp(9)")
    private LocalDateTime correoEnviadoEn;
}
