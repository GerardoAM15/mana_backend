# Archivos de comprobantes electrónicos

Archivos agregados o modificados para esta implementación. Los cambios previos del usuario en `docs/mana-drop.sql`, `JwtService.java`, `JwtServiceTest.java` y la migración `docs/mana-v5-comprobantes.sql` se conservaron.

- `README.md`
- `application-local.example.yaml`
- `docs/COMPROBANTES-ARCHIVOS.md`
- `docs/ENDPOINTS.md`
- `docs/openapi.json`
- `postman/MANA.postman_collection.json`
- `src/main/java/pe/edu/upeu/mana/config/FacturacionProperties.java`
- `src/main/java/pe/edu/upeu/mana/config/SecurityConfig.java`
- `src/main/java/pe/edu/upeu/mana/controller/ComprobanteController.java`
- `src/main/java/pe/edu/upeu/mana/dto/comprobante/ComprobanteDetalleResponseDTO.java`
- `src/main/java/pe/edu/upeu/mana/dto/comprobante/ComprobanteRequestDTO.java`
- `src/main/java/pe/edu/upeu/mana/dto/comprobante/ComprobanteResponseDTO.java`
- `src/main/java/pe/edu/upeu/mana/dto/contrato/ContratoRequestDTO.java`
- `src/main/java/pe/edu/upeu/mana/dto/contrato/RenovarContratoRequestDTO.java`
- `src/main/java/pe/edu/upeu/mana/dto/venta/PagosVentaRequestDTO.java`
- `src/main/java/pe/edu/upeu/mana/dto/venta/VentaRequestDTO.java`
- `src/main/java/pe/edu/upeu/mana/entity/Comprobante.java`
- `src/main/java/pe/edu/upeu/mana/entity/ComprobanteDetalle.java`
- `src/main/java/pe/edu/upeu/mana/entity/SerieComprobante.java`
- `src/main/java/pe/edu/upeu/mana/enums/AmbienteFacturacion.java`
- `src/main/java/pe/edu/upeu/mana/enums/EstadoComprobante.java`
- `src/main/java/pe/edu/upeu/mana/enums/Permiso.java`
- `src/main/java/pe/edu/upeu/mana/enums/TipoComprobante.java`
- `src/main/java/pe/edu/upeu/mana/enums/TipoSerie.java`
- `src/main/java/pe/edu/upeu/mana/enums/TipoSolicitudComprobante.java`
- `src/main/java/pe/edu/upeu/mana/mapper/ComprobanteMapper.java`
- `src/main/java/pe/edu/upeu/mana/mapper/ComprobantePayloadMapper.java`
- `src/main/java/pe/edu/upeu/mana/repository/ComprobanteDetalleRepository.java`
- `src/main/java/pe/edu/upeu/mana/repository/ComprobanteRepository.java`
- `src/main/java/pe/edu/upeu/mana/repository/SerieComprobanteRepository.java`
- `src/main/java/pe/edu/upeu/mana/security/PermisosPorRol.java`
- `src/main/java/pe/edu/upeu/mana/service/ApisPeruFacturacionCliente.java`
- `src/main/java/pe/edu/upeu/mana/service/CalculadoraComprobante.java`
- `src/main/java/pe/edu/upeu/mana/service/ComprobanteReservado.java`
- `src/main/java/pe/edu/upeu/mana/service/CorrelativoComprobante.java`
- `src/main/java/pe/edu/upeu/mana/service/LectorAmbienteEmpresa.java`
- `src/main/java/pe/edu/upeu/mana/service/ProcesadorComprobantes.java`
- `src/main/java/pe/edu/upeu/mana/service/ValidadorReceptorComprobante.java`
- `src/main/java/pe/edu/upeu/mana/service/VerificadorAmbienteFacturacion.java`
- `src/main/java/pe/edu/upeu/mana/service/impl/ComprobanteServiceImpl.java`
- `src/main/java/pe/edu/upeu/mana/service/impl/ContratoServiceImpl.java`
- `src/main/java/pe/edu/upeu/mana/service/impl/EnvioComprobanteServiceImpl.java`
- `src/main/java/pe/edu/upeu/mana/service/impl/ReservaComprobanteServiceImpl.java`
- `src/main/java/pe/edu/upeu/mana/service/impl/VentaServiceImpl.java`
- `src/main/java/pe/edu/upeu/mana/service/service/ComprobanteService.java`
- `src/main/java/pe/edu/upeu/mana/service/service/EnvioComprobanteService.java`
- `src/main/java/pe/edu/upeu/mana/service/service/ReservaComprobanteService.java`
- `src/main/resources/application.yaml`
- `src/test/java/pe/edu/upeu/mana/ComprobanteCobroOracleTest.java`
- `src/test/java/pe/edu/upeu/mana/ComprobanteCorrelativoOracleTest.java`
- `src/test/java/pe/edu/upeu/mana/security/PermisosPorRolTest.java`
- `src/test/java/pe/edu/upeu/mana/service/ComprobanteEventosTest.java`
- `src/test/java/pe/edu/upeu/mana/service/ComprobanteFundamentosTest.java`
- `src/test/java/pe/edu/upeu/mana/service/ComprobanteHttpTest.java`
- `src/test/java/pe/edu/upeu/mana/service/ContratoServiceImplTest.java`
- `src/test/java/pe/edu/upeu/mana/service/EnvioComprobanteTest.java`
- `src/test/java/pe/edu/upeu/mana/service/FacturacionAmbienteTest.java`
- `src/test/java/pe/edu/upeu/mana/service/ReservaComprobanteTest.java`
- `src/test/java/pe/edu/upeu/mana/service/VentaServiceImplTest.java`
- `tools/generar-api.mjs`

## Verificación

- `mvnw.cmd -Dmaven.repo.local=C:/Users/gerar/.m2/repository -o clean package`: BUILD SUCCESS; 224 pruebas reportadas, 0 fallos, 0 errores, 23 omitidas por configuración Oracle optativa.
- Con `MANA_ORACLE_TEST=true`, las pruebas nuevas `ComprobanteCorrelativoOracleTest` y `ComprobanteCobroOracleTest` pasaron contra Oracle local: validación del esquema existente, 20 correlativos concurrentes sin duplicados, cobro confirmado ante aceptación, rechazo, API caída y discrepancia, y nota de crédito al anular.
- `MockRestServiceServer` simula todas las consultas y envíos de facturación. No se llamó a APIsPERU real.
- También se ejecutó toda la suite optativa Oracle: tres pruebas anteriores requieren que exista el administrador de `app.admin.email` con la clave configurada (dos búsquedas sin usuario y un login 401). No se alteraron identidades ni credenciales para satisfacerlas.
- `node tools/generar-api.mjs`: 113 operaciones documentadas; OpenAPI y colección Postman válidos.
- Sin DDL, frontend ni commits.

La configuración y las variables de entorno se describen en la sección **Comprobantes electrónicos** del [README](../README.md).
