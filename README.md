# MANÁ — Backend

API para pensiones prepagadas, consumos, punto de venta, caja, menú, compras de bebidas, inventario, gastos y reportes. Java 21, Spring Boot **4.0.7**, Oracle y paquete `pe.edu.upeu.mana`.

## Equipo y módulos

Proyecto desarrollado en conjunto por los cinco integrantes, trabajando en un mismo equipo de cómputo.
Para que el historial refleje la responsabilidad de cada uno, el código se organizó en commits por módulo:
cada commit tiene como autor al responsable del módulo y como coautores al resto del equipo.

| Integrante | Módulo | Casos de uso |
|---|---|---|
| Gerardo Alarcon | Estructura base, ventas y facturación | CU_07, CU_08, CU_09, CU_26 y consulta DNI/RUC |
| Michael Rodriguez | Pensiones | CU_01 a CU_06 |
| Jyon Yefer Huaccha Callocsa | Menú y planes | CU_11 a CU_14, CU_22, CU_23 |
| Ian Saul Sucapuca Espinoza | Seguridad y caja | CU_10, CU_18 a CU_20, CU_24 |
| Jair Apaza | Inventario y reportes | CU_15 a CU_17, CU_21, CU_25 |

Los casos de uso siguen la numeración de la Matriz de Requerimientos v8.

## Ejecutar

La raíz Maven es esta carpeta, `mana_backend/`, dentro del directorio abierto en IntelliJ. El esquema debe existir previamente y corresponder a [docs/mana-hibrido-v2.sql](docs/mana-hibrido-v2.sql). La aplicación **solo valida** el esquema: no ejecuta ese script, ALTER, migraciones ni DDL. Flyway está deshabilitado y no tiene dependencia.

```powershell
# Desde esta carpeta, con JDK 21 configurado
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
# Alternativa:
java -jar target/mana-backend-1.0.0.jar
```

En Linux/macOS: `./mvnw clean package` y `./mvnw spring-boot:run`. Si la configuración local de Maven apunta a una caché sin permisos, puede indicarse `-Dmaven.repo.local=<directorio-escribible>` sin modificar el proyecto.

Antes de arrancar, configurar `DB_PASSWORD` y `JWT_SECRET` y, para crear el primer dueño, `MANA_ADMIN_PASSWORD`. El repositorio no incluye contraseñas ni secretos JWT de desarrollo. Como alternativa local, copiar `application-local.example.yaml` a `application-local.yaml` en esta carpeta y completar sus valores. Este último archivo está ignorado por Git y el perfil `dev` lo importa opcionalmente. Las variables de un archivo `.env` no se cargan automáticamente en Spring.

Perfil predeterminado: `dev`. Oracle: `jdbc:oracle:thin:@//localhost:1521/xepdb1`. API: `http://localhost:8080/api/v1`. Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). Health público, como PharmaSoft: `GET /api/health`, respuesta `Backend funcionando correctamente`. Health verifica el proceso HTTP; no reemplaza una prueba de conexión a Oracle.

## Variables de entorno

| Variable | Uso | Desarrollo |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` o `prod` | `dev` |
| `DB_URL` | URL JDBC Oracle | `jdbc:oracle:thin:@//localhost:1521/xepdb1` |
| `DB_USERNAME` | Usuario Oracle | `MANADB` |
| `DB_PASSWORD` | Contraseña Oracle | Configurar por entorno o en `application-local.yaml` |
| `JWT_SECRET` | Secreto Base64 de al menos 32 bytes | Configurar por entorno o en `application-local.yaml`; obligatorio en producción |
| `SERVER_PORT` | Puerto HTTP | `8080` |
| `CORS_ORIGINS` | Orígenes permitidos separados por coma | `http://localhost:4200` |
| `APP_ZONA_HORARIA` | Zona del `Clock` | `America/Lima`; configurar ese mismo valor en producción |
| `APP_FRONTEND_URL` | Base del enlace de recuperación | `http://localhost:4200` |
| `MANA_ADMIN_EMAIL` | Correo del primer dueño | `admin@mana.pe` |
| `MANA_ADMIN_PASSWORD` | Clave del primer dueño | Configurar por entorno o en `application-local.yaml` |
| `MANA_ADMIN_DNI` | DNI de ocho dígitos | `00000001` |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT` | Servidor SMTP | Sin SMTP por defecto |
| `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | Credenciales SMTP | Sin valores |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | Autenticación SMTP | Según el servidor |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | TLS SMTP | Según el servidor |
| `APISPERU_TOKEN` | Token de APIsPERU para autocompletar DNI/RUC | Sin token: la consulta responde 503 y se escribe a mano |
| `APISPERU_URL` | Base de la API DNI/RUC | `https://dniruc.apisperu.com/api/v1` |
| `MANA_ORACLE_TEST` | Habilita los tests de integración reales | Ausente: se omiten |

`prod` exige credenciales de BD y JWT por entorno y no importa el archivo local del perfil `dev`. Para personalizar SMTP también pueden proporcionarse las propiedades estándar de Spring Mail mediante configuración externa. En desarrollo sin SMTP, la recuperación escribe el enlace en el log, según el requisito del proyecto; en los demás perfiles no escribe el token.

## Servicios externos

**Correo con Gmail (RF40).** `application-local.example.yaml` trae el bloque `spring.mail` listo para `smtp.gmail.com:587` con STARTTLS. En la cuenta Gmail active la verificación en 2 pasos y genere una *contraseña de aplicación* (Cuenta de Google > Seguridad > Contraseñas de aplicaciones); úsela en `GMAIL_CLAVE_APP` y el correo en `GMAIL_USUARIO`. El remitente es esa misma cuenta. Un fallo de envío se registra en el log sin cambiar la respuesta de `/auth/recuperar`.

**Consulta DNI/RUC (APIsPERU).** `GET /api/v1/consultas/dni/{dni}` (permiso `CLIENTE_ESCRIBIR` o `USUARIO_GESTIONAR`) devuelve nombre y apellido; si la persona ya existe en MANÁ responde desde la BD con `registrado=true` sin gastar consulta. `GET /api/v1/consultas/ruc/{ruc}` (`INVENTARIO_GESTIONAR`) devuelve razón social, dirección, teléfono, estado y condición de SUNAT. El token solo vive en el backend y no se escribe en el log; las respuestas se guardan 12 horas en memoria y cada usuario puede hacer 30 consultas por minuto (429 al exceder). Respuestas: 400 formato inválido, 404 no encontrado, 503 API sin token, caída o lenta (3 s para conectar, 5 s de lectura). El registro nunca depende de la API: el formulario se completa a mano.

## Primer dueño y autenticación

Al arrancar, si no existe ningún usuario `DUENO`, `AdministradorBootstrap` crea persona, empleado y usuario con rol `DUENO` en el primer local activo. Conserva las propiedades `app.admin.email`, `app.admin.password` y `app.admin.dni` y las variables `MANA_ADMIN_*`. Si faltan valores, avisa y no crea nada. Si DNI o correo ya están en uso, avisa y no duplica registros. Los usuarios existentes conservan su rol y contraseña: si el administrador inicial ya existe y aún no hay dueño, configure una identidad libre en `MANA_ADMIN_EMAIL` y `MANA_ADMIN_DNI` para crear al primer dueño.

```http
POST /api/v1/auth/login
Content-Type: application/json

{"correo":"admin@mana.pe","clave":"<contraseña configurada>"}
```

El login devuelve `token`, `tipo`, `expiraEnSegundos`, `usuarioId`, `nombreCompleto`, `rol`, `localId`, `localNombre` y `permisos`. Enviar `Authorization: Bearer <token>`. `/auth/me` devuelve la identidad sin token. JWT dura una hora y contiene `sub` y `tv`; cada petición vuelve a comprobar usuario, empleado y versión del token. Los cambios de contraseña, rol, estado o local invalidan los tokens anteriores.

`POST /auth/recuperar` devuelve el mismo 200 para cualquier correo válido. El token aleatorio tiene 32 bytes, se guarda únicamente como SHA-256, vence en cinco minutos y se usa una sola vez. `POST /auth/restablecer` recibe `token` y `nuevaClave`.

## Permisos

Jerarquía: **Dueño > Administrador > Cajero** (códigos de rol `DUENO`, `ADMINISTRADOR` y `CAJERO`). El dueño tiene todos los permisos; el administrador tiene todos excepto `LOCAL_GESTIONAR`.

| Permiso | Dueño | Administrador | Cajero |
|---|:---:|:---:|:---:|
| USUARIO_GESTIONAR | ✓ | ✓ | |
| LOCAL_GESTIONAR | ✓ | | |
| CLIENTE_LEER | ✓ | ✓ | ✓ |
| CLIENTE_ESCRIBIR | ✓ | ✓ | ✓ |
| CONTRATO_LEER | ✓ | ✓ | ✓ |
| CONTRATO_REGISTRAR | ✓ | ✓ | |
| CONTRATO_ANULAR | ✓ | ✓ | |
| CONSUMO_REGISTRAR | ✓ | ✓ | ✓ |
| CONSUMO_ANULAR | ✓ | ✓ | |
| VENTA_REGISTRAR | ✓ | ✓ | ✓ |
| VENTA_ANULAR | ✓ | ✓ | |
| CAJA_OPERAR | ✓ | ✓ | ✓ |
| CAJA_SUPERVISAR | ✓ | ✓ | |
| MENU_GESTIONAR | ✓ | ✓ | |
| MENU_LEER | ✓ | ✓ | ✓ |
| PLAN_GESTIONAR | ✓ | ✓ | |
| CALENDARIO_GESTIONAR | ✓ | ✓ | |
| INVENTARIO_GESTIONAR | ✓ | ✓ | |
| GASTO_GESTIONAR | ✓ | ✓ | |
| REPORTE_VER | ✓ | ✓ | |

La identidad determina el local operativo a partir del empleado. Los filtros `localId` de otras sedes se permiten al dueño y al administrador. Las lecturas, reportes y escrituras comprueban ese alcance. Quien abrió la caja, un administrador o un dueño pueden cerrarla; los dos últimos también pueden cobrar y anular ventas de otras cajas.

Un `ADMINISTRADOR` solo crea, edita, desactiva y cambia el rol de usuarios `CAJERO`, y únicamente puede asignar `CAJERO`. Un dueño gestiona todos los roles. Una operación fuera de esa jerarquía devuelve 403: `No tiene permiso para gestionar este usuario`. `UsuarioResponseDTO.gestionable` indica si la jerarquía permite editar al usuario; un dueño puede editar su propio perfil, pero nadie puede cambiarse el rol ni desactivarse a sí mismo. El frontend debe mantener esas dos acciones deshabilitadas para la propia cuenta.

Desactivar al último dueño activo o quitarle el rol `DUENO`, tanto por edición como por los endpoints específicos, devuelve 409: `Debe existir al menos un dueño activo`. Esta protección tiene prioridad si también se trata de la propia cuenta. Las operaciones de gestión y el bootstrap comparten un bloqueo del rol `DUENO` antes de bloquear al usuario, para que dos peticiones concurrentes no eliminen al último dueño.

## Arquitectura vigente

La referencia es **`../referencia/pharmasoft/`**, fuera del build. Tiene prioridad sobre AutoAndes. Se conservan las reglas de negocio de [docs/prompt-backend-mana.md](docs/prompt-backend-mana.md) y el esquema actualizado; no se incorpora Flyway.

- `enums/`: todos los enums, incluido `Permiso`; cualquier enum nuevo debe ir aquí.
- `entity/`, `repository/`, `mapper/`: persistencia y conversión manual a DTOs, sin MapStruct ni entidades devueltas por controladores.
- `service/service/`: interfaces; `service/impl/`: implementaciones. Los componentes de cálculo y validación permanecen en `service/`.
- `dto/<modulo>/`: clases Lombok con getters, setters y constructores sin argumentos y con todos los argumentos. `PaginaResponseDTO` conserva su ubicación y contrato de PharmaSoft.
- `exception/dto/ErrorResponseDTO`: `timestamp`, `status`, `error`, `message`, `path`, `validationErrors`.
- `security/`: JWT, filtro, permisos por rol y handlers 401/403. `config/` contiene las configuraciones.
- `util/PaginacionUtil`, `controller/`, `scheduler/ContratoScheduler`.

Inyección mediante constructores explícitos, transacciones por método, lecturas con `readOnly=true` y logs de inicio/fin con `duracionMs`. `Clock` inyectado en zona Lima para fechas del negocio; la duración usa el reloj monotónico. Auditoría por usuario autenticado o `sistema`, bloqueo optimista y bloqueos pesimistas para caja, correlativos, clientes, detalle de contrato y stock. Las bebidas se bloquean en orden para serializar la creación de stock faltante; las filas de stock existentes se bloquean por id ascendente.

## Paginación y errores

Clientes, contratos, ventas, consumos, movimientos de stock, gastos y cajas admiten `pagina=0&tamanio=20&ordenarPor=id&direccion=desc`. El tamaño permitido es 1–100; la página comienza en cero. La paginación y el conteo se ejecutan en Oracle. Los catálogos pequeños permanecen como listas.

`GET /api/v1/clientes` acepta además `q`: busca DNI exacto o texto contenido en nombre/apellido, sin distinguir mayúsculas. Se recortan espacios exteriores y se tratan `%`, `_` y `!` como texto literal. Omitir `q` o enviarlo vacío conserva el listado completo y su paginación.

```json
{"contenido":[],"pagina":0,"tamanio":20,"totalElementos":0,"totalPaginas":0,"ultima":true}
```

| Listado | Campos ordenables |
|---|---|
| clientes | id, dni, nombre, apellido, activo |
| contratos | id, numero, fechaInicio, fechaFin, montoTotal, estado |
| ventas | id, numero, fecha, total, estado |
| consumos | id, fecha, registradoEn, anulado |
| movimientos-stock | id, fecha, cantidad, tipo, bebidaId |
| gastos | id, fecha, monto, activo |
| cajas | id, apertura, cierre, usuarioId, montoInicial |

Página, tamaño, dirección o campo de ordenación fuera de la lista permitida producen 409, igual que en PharmaSoft. Los errores de validación, tipos o JSON producen 400; autenticación 401; permisos 403; recurso inexistente 404; reglas, duplicados o concurrencia 409; imprevistos 500 sin detalles internos.

```json
{"timestamp":"2026-11-03T12:00:00","status":400,"error":"Bad Request","message":"Datos invalidos","path":"/api/v1/clientes","validationErrors":{"dni":"El DNI debe tener 8 dígitos"}}
```

## Reglas de negocio

- Pensiones exclusivamente prepagadas. Semanal: inicio + 6 días; mensual: inicio + un mes − un día. Las raciones se calculan con atención ISO del servicio y cierres del local. D/A lunes–viernes; C lunes–jueves según los datos de la BD.
- Simular no guarda datos. Registrar requiere cliente y plan activos, servicios del plan, inicio no anterior a hoy, ausencia de superposición en cualquier local, caja abierta y pagos que sumen exactamente el total. Se congelan precios y cantidades; cambiar tarifas no altera contratos existentes.
- Renovar solo un contrato activo en sus últimos tres días; crea otro desde el día posterior a su fin, sin trasladar saldo. El anterior sigue vigente hasta su fin. El scheduler actualiza estados al arrancar y a las 00:05 de Lima; las consultas calculan el estado correcto por fechas mientras tanto.
- Anular un contrato exige motivo de 10–250 caracteres y ausencia de cualquier consumo, incluso anulado. Devuelve cada cobro por el mismo método, en la caja abierta del usuario y del mismo local.
- Un consumo por cliente, fecha y servicio. Se exige atención, contrato vigente, saldo y composición exacta del menú programado de hoy. Descuenta una ración y conserva su valor pactado. Al anular hoy se devuelve la ración, se conserva la auditoría y se libera la clave diaria. Un recargo exige caja abierta y método de pago; su anulación exige que la caja original siga abierta.
- Toda tarifa, precio de combo, suelto y bebida se lee de la BD. D: líquido + dos panes; A: entrada + segundo + refresco; C: segundo + refresco, según `componentes_regla`. Para consumos debe existir una regla activa inequívoca por servicio.
- Al crear, actualizar o activar una regla de cobro se permite como máximo un menú activo por servicio. Un conflicto devuelve 409: `Ya existe un menú activo para ese servicio; desactívelo primero`. Las reglas inactivas pueden coexistir y editar la propia regla activa no se considera duplicado.
- Una línea COMBO describe la composición de una unidad; su `cantidad` multiplica esa elección. Las bebidas de inventario no llevan recargo. Platos y combos deben estar programados hoy. Las bebidas descuentan stock al registrar, aun si la venta está pendiente.
- Cobrar exige venta pendiente, caja original abierta y pagos por el total exacto. Cobrar y anular solo se permiten al propietario de esa caja, a un administrador o a un dueño; otro usuario recibe 403. Anular repone bebidas y devuelve cada pago si la venta estaba pagada.
- Una caja abierta por usuario. El cierre rechaza ventas pendientes y guarda arqueo por método, incluso cuando los movimientos suman cero. Métodos omitidos en `conteos` equivalen a contado cero; los repetidos se rechazan.
- **Esperado** = monto inicial solo en EFECTIVO + cobros de ventas − devoluciones de ventas + cobros de contratos − devoluciones de contratos + recargos de consumos no anulados. El esperado puede ser negativo, tanto por método como en total; el contado nunca es negativo. `diferencia = contado − esperado`.
- **Devoluciones en EFECTIVO de contratos o ventas**: el efectivo esperado actual, incluido el monto inicial, debe cubrir el total a devolver. Si no, 409: `No hay efectivo suficiente en caja para la devolución`. No hay límite equivalente para otros métodos.
- Compras: solo bebidas ofrecidas por el proveedor, activo. Se suman `presentaciones × unidadesPorPresentacion` y se registra COMPRA; las ventas y sus devoluciones registran VENTA/DEVOLUCION. Los movimientos registran la hora de operación; la compra también conserva su fecha comercial.
- Bajas lógicas para clientes, usuarios, platos, bebidas, proveedores, planes, reglas, tarifas, precios y gastos. Los datos históricos permanecen.

## Reportes

Todos aplican alcance local. `desde` y `hasta` incluyen ambos días. Ingresos y comparativo muestran periodos mensuales; gastos admite `agrupacion=DIA` o `MES`. Los cobros y devoluciones se agrupan por su propia fecha, de modo que una anulación no borra un ingreso de meses anteriores. La columna `contratos` representa los ingresos de pensiones: cobros netos de contratos más recargos positivos de consumos no anulados. Los recargos se agrupan por `registrado_en` y por el local de su sesión de caja, tanto en ingresos mensuales como en gastos vs. ingresos. Gastos desactivados y consumos anulados se excluyen de sus sumas.

`contratos-por-vencer` admite `dias=3` (0–366), y opcionalmente `desde`/`hasta` para acotar la ventana de vencimientos; devuelve una fila por contrato y servicio, con saldo. Los platos más consumidos consideran consumos pensionistas no anulados. Asistencia agrupa por fecha y servicio.

## Tests y contrato de API

```powershell
# Unitarios; no necesita Oracle
.\mvnw.cmd clean package

# Integración real y validación del esquema
$env:MANA_ORACLE_TEST='true'
.\mvnw.cmd clean package
Remove-Item Env:MANA_ORACLE_TEST
```

Los tests de integración de negocio usan transacciones que revierten los datos de prueba. Como cualquier arranque, el contexto puede crear el primer dueño y actualizar estados vencidos mediante el bootstrap/scheduler. La conexión real valida todas las entidades; se prueban raciones, superposición, renovaciones, consumo doble, saldo, menú, caja, devoluciones, totales, compras, kardex, reportes, JWT, jerarquía de roles, gestión de usuarios, protección del último dueño, errores HTTP y paginación. SMTP requiere configuración externa y no se prueba con correos reales.

El test HTTP exporta `target/openapi.json`. [docs/openapi.json](docs/openapi.json) es una instantánea generada del contrato real. Tras cambiar endpoints, ejecutar la integración y luego `node tools/generar-api.mjs` para actualizar la documentación y la colección; Node se utiliza solo para esa generación, no para ejecutar la API.

Importar [postman/MANA.postman_collection.json](postman/MANA.postman_collection.json). Configurar `correo`, `clave`, ids y fechas. Login guarda `{{token}}`; la simulación guarda `montoContrato`; crear venta guarda `ventaId` y `montoVenta`. Las peticiones heredan Bearer. Los ejemplos no son datos iniciales adicionales ni una carga automática: ajustar importes según la simulación o el catálogo y ejecutar únicamente las operaciones elegidas. La colección contiene operaciones que crean, editan y anulan datos reales.

## Comprobantes electrónicos

Boletas, facturas y notas de crédito para ventas pagadas y contratos prepagados.
Requiere el esquema existente de [docs/mana-v5-comprobantes.sql](docs/mana-v5-comprobantes.sql).
La aplicación mantiene `ddl-auto: validate`: no crea ni modifica tablas.

### Configuración y ambiente

Por defecto `app.facturacion.habilitada=false`: el cobro conserva el ticket interno
sin emisión electrónica; los endpoints de emitir y reenviar responden 409
`La facturación electrónica no está habilitada`.

Copiar el bloque comentado de `application-local.example.yaml` al archivo ignorado
`application-local.yaml`. El token es exclusivo del backend; nunca se registra en logs.
La empresa debe estar registrada, con certificado y credenciales SUNAT configurados
en el panel de APIsPERU. No se almacenan certificados en este repositorio.

| Variable | Valor predeterminado / uso |
|---|---|
| `FACTURACION_HABILITADA` | `false`; activar después de configurar empresa y series |
| `FACTURACION_AMBIENTE` | `BETA` o `PRODUCCION`; predeterminado `BETA` |
| `FACTURACION_URL` | `https://facturacion.apisperu.com/api/v1` |
| `FACTURACION_TOKEN` | Token Bearer de APIsPERU; sin valor predeterminado |
| `FACTURACION_COMPANY_ID` | Identificador de empresa, consultado solo en lectura |
| `FACTURACION_IGV` | `18.00`; **confirmar la tasa aplicable con un contador** |
| `FACTURACION_BOLETA_MONTO_IDENTIFICACION` | `700.00` |
| `FACTURACION_EMPRESA_RUC` | RUC del emisor |
| `FACTURACION_EMPRESA_RAZON_SOCIAL` | Razón social |
| `FACTURACION_EMPRESA_NOMBRE_COMERCIAL` | Nombre comercial |
| `FACTURACION_EMPRESA_DIRECCION` | Dirección fiscal |
| `FACTURACION_EMPRESA_UBIGEO` | Ubigeo fiscal, seis dígitos |

**BETA es solo para pruebas SUNAT, sin validez tributaria.** El ambiente se cambia
**manualmente en el panel de APIsPERU** y `app.facturacion.ambiente` debe coincidir:
`BETA` acepta `beta` y `nubefact_beta`; `PRODUCCION` acepta `produccion` y
`nubefact_produccion`. MANÁ solo consulta la empresa; nunca la crea ni modifica.
Para producción configurar ambiente, token, empresa y series correspondientes.
Los comprobantes pendientes de otro ambiente no se envían al nuevo ambiente.

Al arrancar se verifica el ambiente y se guarda el resultado durante 10 minutos;
antes de enviar se renueva si venció. Una discrepancia deja el comprobante
`PENDIENTE` con el mensaje explicativo. Emitir manualmente o reenviar responde 409
con ese mensaje **después de guardar la reserva**. El cobro o contrato se conserva.
Una caída, timeout o ambiente no determinable queda pendiente para reintento,
sin clasificarse como discrepancia.

El OpenAPI declara `environment` como objeto sin propiedades. `LectorAmbienteEmpresa`
acepta texto conocido o busca recursivamente valores de texto del objeto. Exige un
único valor distinto entre los cuatro documentados. Un objeto ambiguo o desconocido
genera WARN con **solo** el JSON de `environment`; queda el TODO para reemplazarlo
por el campo exacto cuando exista una respuesta real del proveedor.

### Solicitudes, montos y estados

`VentaRequestDTO`, `PagosVentaRequestDTO`, `ContratoRequestDTO` y
`RenovarContratoRequestDTO` aceptan este bloque opcional:

```json
{"comprobante":{"tipo":"BOLETA","receptorTipoDoc":"1","receptorNumDoc":"12345678","receptorNombre":"Cliente","receptorDireccion":"Dirección"}}
```

Sin bloque o con `tipo: TICKET`, el flujo sigue usando ticket interno. Una factura
requiere RUC válido y razón social (si falta, se consulta con el servicio DNI/RUC).
Una boleta menor al umbral admite `CLIENTES VARIOS`; al alcanzar el umbral requiere
DNI o RUC. Una venta o contrato tiene como máximo un comprobante vigente.

Los precios incluyen IGV. Se descompone cada línea y se ajusta el redondeo de la
última para cuadrar base, IGV y total. Ventas incluyen el recargo para llevar;
contratos usan una línea por servicio y unidad `ZZ`. Receptor y líneas quedan
congelados, aunque después cambien el cliente o los precios.

La serie se bloquea con `PESSIMISTIC_WRITE` y se reserva el número dentro de la
transacción del cobro: formato `B001-00000001`. Debe existir exactamente una serie
activa de cada tipo utilizado por local; los números no se reinician al cambiar
el ambiente. El envío usa un evento `AFTER_COMMIT` y una transacción nueva. Un fallo
de facturación nunca revierte una venta confirmada.

Estados: `PENDIENTE` (incluye ambiente no verificable), `ACEPTADO`, `OBSERVADO`
(aceptado con notas del CDR), `RECHAZADO` (CDR no aceptado, o error SUNAT 2000–3999),
`ERROR` (API caída o excepción SUNAT sin CDR, códigos 0100–1999) y `ANULADO`. Se
conservan código, mensaje, hash, XML y CDR base64.
Cada cinco minutos se reintentan pendientes y errores, hasta cinco intentos de
**envío**. Las verificaciones de ambiente bloqueadas no consumen intentos de envío.
Los bloqueos del comprobante serializan reintentos y anulaciones.

Casos especiales:

- **Rechazado:** no tiene validez tributaria y no se reintenta (reenviar los mismos
  datos no sirve). Al emitir de nuevo para esa venta o contrato (`POST
  /comprobantes/ventas/{id}` o `/contratos/{id}`, p. ej. con el DNI o RUC corregido),
  el rechazado se marca `ANULADO` sin nota de crédito y se emite otro con un nuevo número.
- **Código 1033 (ya registrado):** un envío anterior llegó a SUNAT aunque se perdió la
  respuesta. No se reenvía: se consulta el CDR con `GET /invoice/status` y se aplica.
  Si no hay CDR (o es una nota de crédito), queda `ERROR` sin más reintentos y con un
  mensaje para verificarlo a mano en SUNAT o en el panel de APIsPERU.
- **Correo al cliente:** si la solicitud trae `receptorEmail`, cuando SUNAT acepta el comprobante se envía
  el PDF y el XML firmado con el SMTP de `spring.mail` (el mismo de la recuperación de contraseña).
  También se puede enviar o reenviar con `POST /comprobantes/{id}/correo` (`{ "email": "..." }`, opcional).
  Requiere `docs/mana-v6-comprobante-correo.sql` (columnas `receptor_email` y `correo_enviado_en`).

Al anular una operación con comprobante aceptado u observado se reserva una nota
de crédito con motivo `01`, `Anulación de la operación`, mismos datos y montos,
serie de notas de boleta (`BC01`) o factura (`FC01`) configurada en su local.
El original se marca `ANULADO`; la nota tiene su propio estado y reintentos.
Si el original estaba pendiente, con error o rechazado, solo se marca anulado.

### Endpoints y permisos

Prefijo `/api/v1/comprobantes`. Todos devuelven DTO salvo PDF y XML.

| Método | Ruta | Uso |
|---|---|---|
| GET | `/` | Página; filtros `desde`, `hasta`, `tipo`, `estado`, `localId`, `pagina`, `tamanio` |
| GET | `/{id}` | Detalle con líneas, respuesta SUNAT y referencia |
| GET | `/{id}/pdf` | PDF de APIsPERU, `application/pdf` |
| GET | `/{id}/xml` | XML firmado guardado, `application/xml`; 404 si aún no existe |
| POST | `/{id}/reenviar` | Reintento de un pendiente/error con menos de cinco intentos |
| GET | `/ventas/{ventaId}` | Comprobante vigente; 404 si no existe |
| GET | `/contratos/{contratoId}` | Comprobante vigente; 404 si no existe |
| POST | `/ventas/{ventaId}` | Emitir para venta pagada; cuerpo directo con los campos del bloque `comprobante` |
| POST | `/contratos/{contratoId}` | Emitir para contrato; mismo cuerpo |

`COMPROBANTE_VER` y `COMPROBANTE_EMITIR`: dueño, administrador y cajero.
`COMPROBANTE_GESTIONAR`: dueño y administrador, para reenviar y listar todos los
locales sin filtro. El cajero ve y emite solo en su local. Se aplica
`ContextoLocalService`. El DTO incluye los datos para que el frontend arme el QR:
`rucEmisor`, `tipoSunat`, serie, número, IGV, total, fecha, documento receptor y hash.

Contrato externo verificado en la [documentación oficial](https://facturacion.apisperu.com/doc)
y [OpenAPI 1.3](https://facturacion.apisperu.com/doc/swagger). Autenticación HTTP Bearer JWT.
Endpoints utilizados por el cliente (rutas relativas a la base configurada):

- `GET /companies/{companyId}`: consulta de ambiente.
- `POST /invoice/send`: envío de boleta/factura; respuesta `xml`, `hash`,
  `sunatResponse.cdrZip` y `sunatResponse.cdrResponse`.
- `POST /note/send`: envío de nota de crédito, incluyendo los campos oficiales
  `tipDocAfectado`, `numDocfectado`, `codMotivo` y `desMotivo`.
- `POST /invoice/pdf`, `POST /note/pdf`: PDF a partir de las líneas guardadas.

El XML servido es el firmado que devuelve el envío; no se regenera ni se vuelve a
firmar con `/invoice/xml` o `/note/xml`. Ningún test llama a APIsPERU real. Las
pruebas Oracle (`MANA_ORACLE_TEST=true`) validan el esquema existente, los números
concurrentes y el cobro confirmado con respuestas de API simuladas.

## Endpoints

La tabla enlaza un ejemplo de request y response para **cada operación**. [docs/ENDPOINTS.md](docs/ENDPOINTS.md) incluye también sus parámetros. Los ejemplos son ilustrativos y deben adaptarse a los ids y precios de la instalación.

<!-- ENDPOINTS -->

| Método | Ruta | Permiso | Ejemplos |
|---|---|---|---|
| GET | `/api/health` | Público | [Request / response](docs/ENDPOINTS.md#operacion-001) |
| POST | `/api/v1/auth/login` | Público | [Request / response](docs/ENDPOINTS.md#operacion-002) |
| GET | `/api/v1/auth/me` | Autenticado | [Request / response](docs/ENDPOINTS.md#operacion-003) |
| POST | `/api/v1/auth/recuperar` | Público | [Request / response](docs/ENDPOINTS.md#operacion-004) |
| POST | `/api/v1/auth/restablecer` | Público | [Request / response](docs/ENDPOINTS.md#operacion-005) |
| GET | `/api/v1/bebidas` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-006) |
| POST | `/api/v1/bebidas` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-007) |
| GET | `/api/v1/bebidas/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-008) |
| PUT | `/api/v1/bebidas/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-009) |
| DELETE | `/api/v1/bebidas/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-010) |
| GET | `/api/v1/cajas` | CAJA_SUPERVISAR | [Request / response](docs/ENDPOINTS.md#operacion-011) |
| POST | `/api/v1/cajas/{id}/cerrar` | CAJA_OPERAR | [Request / response](docs/ENDPOINTS.md#operacion-012) |
| GET | `/api/v1/cajas/{id}/resumen` | CAJA_OPERAR | [Request / response](docs/ENDPOINTS.md#operacion-013) |
| POST | `/api/v1/cajas/abrir` | CAJA_OPERAR | [Request / response](docs/ENDPOINTS.md#operacion-014) |
| GET | `/api/v1/cajas/actual` | CAJA_OPERAR | [Request / response](docs/ENDPOINTS.md#operacion-015) |
| GET | `/api/v1/catalogos` | Autenticado | [Request / response](docs/ENDPOINTS.md#operacion-016) |
| GET | `/api/v1/clientes` | CLIENTE_LEER | [Request / response](docs/ENDPOINTS.md#operacion-017) |
| POST | `/api/v1/clientes` | CLIENTE_ESCRIBIR | [Request / response](docs/ENDPOINTS.md#operacion-018) |
| GET | `/api/v1/clientes/{id}` | CLIENTE_LEER | [Request / response](docs/ENDPOINTS.md#operacion-019) |
| PUT | `/api/v1/clientes/{id}` | CLIENTE_ESCRIBIR | [Request / response](docs/ENDPOINTS.md#operacion-020) |
| DELETE | `/api/v1/clientes/{id}` | CLIENTE_ESCRIBIR | [Request / response](docs/ENDPOINTS.md#operacion-021) |
| GET | `/api/v1/clientes/{id}/contrato-vigente` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-022) |
| GET | `/api/v1/compras-bebidas` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-023) |
| POST | `/api/v1/compras-bebidas` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-024) |
| GET | `/api/v1/compras-bebidas/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-025) |
| GET | `/api/v1/comprobantes` | COMPROBANTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-026) |
| GET | `/api/v1/comprobantes/{id}` | COMPROBANTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-027) |
| GET | `/api/v1/comprobantes/{id}/pdf` | COMPROBANTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-028) |
| POST | `/api/v1/comprobantes/{id}/reenviar` | COMPROBANTE_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-029) |
| GET | `/api/v1/comprobantes/{id}/xml` | COMPROBANTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-030) |
| GET | `/api/v1/comprobantes/contratos/{contratoId}` | COMPROBANTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-031) |
| POST | `/api/v1/comprobantes/contratos/{contratoId}` | COMPROBANTE_EMITIR | [Request / response](docs/ENDPOINTS.md#operacion-032) |
| GET | `/api/v1/comprobantes/ventas/{ventaId}` | COMPROBANTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-033) |
| POST | `/api/v1/comprobantes/ventas/{ventaId}` | COMPROBANTE_EMITIR | [Request / response](docs/ENDPOINTS.md#operacion-034) |
| GET | `/api/v1/consultas/dni/{dni}` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-035) |
| GET | `/api/v1/consultas/ruc/{ruc}` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-036) |
| GET | `/api/v1/consumos` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-037) |
| POST | `/api/v1/consumos` | CONSUMO_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-038) |
| PATCH | `/api/v1/consumos/{id}/anular` | CONSUMO_ANULAR | [Request / response](docs/ENDPOINTS.md#operacion-039) |
| GET | `/api/v1/contratos` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-040) |
| POST | `/api/v1/contratos` | CONTRATO_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-041) |
| GET | `/api/v1/contratos/{id}` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-042) |
| PATCH | `/api/v1/contratos/{id}/anular` | CONTRATO_ANULAR | [Request / response](docs/ENDPOINTS.md#operacion-043) |
| POST | `/api/v1/contratos/{id}/renovar` | CONTRATO_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-044) |
| POST | `/api/v1/contratos/simular` | CONTRATO_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-045) |
| GET | `/api/v1/dias-no-atencion` | CALENDARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-046) |
| POST | `/api/v1/dias-no-atencion` | CALENDARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-047) |
| PUT | `/api/v1/dias-no-atencion/{id}` | CALENDARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-048) |
| DELETE | `/api/v1/dias-no-atencion/{id}` | CALENDARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-049) |
| GET | `/api/v1/gastos` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-050) |
| POST | `/api/v1/gastos` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-051) |
| GET | `/api/v1/gastos/{id}` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-052) |
| PUT | `/api/v1/gastos/{id}` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-053) |
| DELETE | `/api/v1/gastos/{id}` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-054) |
| GET | `/api/v1/locales` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-055) |
| POST | `/api/v1/locales` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-056) |
| GET | `/api/v1/locales/{id}` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-057) |
| PUT | `/api/v1/locales/{id}` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-058) |
| DELETE | `/api/v1/locales/{id}` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-059) |
| GET | `/api/v1/menus` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-060) |
| PUT | `/api/v1/menus` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-061) |
| POST | `/api/v1/menus/clonar` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-062) |
| POST | `/api/v1/menus/copiar-a-cena` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-063) |
| GET | `/api/v1/movimientos-stock` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-064) |
| GET | `/api/v1/pensionistas/buscar` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-065) |
| GET | `/api/v1/planes` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-066) |
| POST | `/api/v1/planes` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-067) |
| GET | `/api/v1/planes/{id}` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-068) |
| PUT | `/api/v1/planes/{id}` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-069) |
| DELETE | `/api/v1/planes/{id}` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-070) |
| GET | `/api/v1/platos` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-071) |
| POST | `/api/v1/platos` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-072) |
| GET | `/api/v1/platos/{id}` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-073) |
| PUT | `/api/v1/platos/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-074) |
| DELETE | `/api/v1/platos/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-075) |
| GET | `/api/v1/pos/catalogo` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-076) |
| GET | `/api/v1/precios-sueltos` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-077) |
| POST | `/api/v1/precios-sueltos` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-078) |
| PUT | `/api/v1/precios-sueltos/{servicio}/{categoria}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-079) |
| DELETE | `/api/v1/precios-sueltos/{servicio}/{categoria}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-080) |
| GET | `/api/v1/proveedores` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-081) |
| POST | `/api/v1/proveedores` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-082) |
| GET | `/api/v1/proveedores/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-083) |
| PUT | `/api/v1/proveedores/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-084) |
| DELETE | `/api/v1/proveedores/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-085) |
| GET | `/api/v1/reglas-cobro` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-086) |
| POST | `/api/v1/reglas-cobro` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-087) |
| GET | `/api/v1/reglas-cobro/{id}` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-088) |
| PUT | `/api/v1/reglas-cobro/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-089) |
| DELETE | `/api/v1/reglas-cobro/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-090) |
| GET | `/api/v1/reportes/asistencia-diaria` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-091) |
| GET | `/api/v1/reportes/contratos-por-vencer` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-092) |
| GET | `/api/v1/reportes/gastos-vs-ingresos` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-093) |
| GET | `/api/v1/reportes/ingresos-mensuales` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-094) |
| GET | `/api/v1/reportes/platos-mas-consumidos` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-095) |
| GET | `/api/v1/reportes/ventas-vs-pensiones` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-096) |
| GET | `/api/v1/stock-bebidas` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-097) |
| GET | `/api/v1/tarifas` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-098) |
| POST | `/api/v1/tarifas` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-099) |
| GET | `/api/v1/tarifas/{id}` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-100) |
| PUT | `/api/v1/tarifas/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-101) |
| DELETE | `/api/v1/tarifas/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-102) |
| GET | `/api/v1/usuarios` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-103) |
| POST | `/api/v1/usuarios` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-104) |
| GET | `/api/v1/usuarios/{id}` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-105) |
| PUT | `/api/v1/usuarios/{id}` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-106) |
| DELETE | `/api/v1/usuarios/{id}` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-107) |
| PATCH | `/api/v1/usuarios/{id}/rol` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-108) |
| GET | `/api/v1/ventas` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-109) |
| POST | `/api/v1/ventas` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-110) |
| GET | `/api/v1/ventas/{id}` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-111) |
| PATCH | `/api/v1/ventas/{id}/anular` | VENTA_ANULAR | [Request / response](docs/ENDPOINTS.md#operacion-112) |
| POST | `/api/v1/ventas/{id}/pagos` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-113) |
