# MANÁ — Backend

API para pensiones prepagadas, consumos, punto de venta, caja, menú, compras de bebidas, inventario, gastos y reportes. Java 21, Spring Boot **4.0.7**, Oracle y paquete `pe.edu.upeu.mana`.

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

Antes de arrancar, configurar `DB_PASSWORD` y `JWT_SECRET` y, para crear el primer administrador, `MANA_ADMIN_PASSWORD`. El repositorio no incluye contraseñas ni secretos JWT de desarrollo. Como alternativa local, copiar `application-local.example.yaml` a `application-local.yaml` en esta carpeta y completar sus valores. Este último archivo está ignorado por Git y el perfil `dev` lo importa opcionalmente. Las variables de un archivo `.env` no se cargan automáticamente en Spring.

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
| `MANA_ADMIN_EMAIL` | Correo del primer administrador | `admin@mana.pe` |
| `MANA_ADMIN_PASSWORD` | Clave del primer administrador | Configurar por entorno o en `application-local.yaml` |
| `MANA_ADMIN_DNI` | DNI de ocho dígitos | `00000001` |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT` | Servidor SMTP | Sin SMTP por defecto |
| `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | Credenciales SMTP | Sin valores |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | Autenticación SMTP | Según el servidor |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | TLS SMTP | Según el servidor |
| `MANA_ORACLE_TEST` | Habilita los tests de integración reales | Ausente: se omiten |

`prod` exige credenciales de BD y JWT por entorno y no importa el archivo local del perfil `dev`. Para personalizar SMTP también pueden proporcionarse las propiedades estándar de Spring Mail mediante configuración externa. En desarrollo sin SMTP, la recuperación escribe el enlace en el log, según el requisito del proyecto; en los demás perfiles no escribe el token.

## Primer administrador y autenticación

Al arrancar, si no existe ningún usuario `ADMINISTRADOR`, el bootstrap crea persona, empleado y usuario en el primer local activo. Usa `app.admin.email`, `app.admin.password` y `app.admin.dni`, alimentados por las variables anteriores. Si faltan valores, avisa y no crea nada. Si DNI o correo ya están en uso, avisa y no duplica registros. Un administrador existente no recibe cambios automáticos de clave.

```http
POST /api/v1/auth/login
Content-Type: application/json

{"correo":"admin@mana.pe","clave":"<contraseña configurada>"}
```

El login devuelve `token`, `tipo`, `expiraEnSegundos`, `usuarioId`, `nombreCompleto`, `rol`, `localId`, `localNombre` y `permisos`. Enviar `Authorization: Bearer <token>`. `/auth/me` devuelve la identidad sin token. JWT dura una hora y contiene `sub` y `tv`; cada petición vuelve a comprobar usuario, empleado y versión del token. Los cambios de contraseña, rol, estado o local invalidan los tokens anteriores.

`POST /auth/recuperar` devuelve el mismo 200 para cualquier correo válido. El token aleatorio tiene 32 bytes, se guarda únicamente como SHA-256, vence en cinco minutos y se usa una sola vez. `POST /auth/restablecer` recibe `token` y `nuevaClave`.

## Permisos

| Permiso | ADMINISTRADOR | DUENO | CAJERO |
|---|:---:|:---:|:---:|
| USUARIO_GESTIONAR | ✓ | | |
| LOCAL_GESTIONAR | ✓ | | |
| CLIENTE_LEER | ✓ | ✓ | ✓ |
| CLIENTE_ESCRIBIR | ✓ | | ✓ |
| CONTRATO_LEER | ✓ | ✓ | ✓ |
| CONTRATO_REGISTRAR | ✓ | | |
| CONTRATO_ANULAR | ✓ | | |
| CONSUMO_REGISTRAR | ✓ | | ✓ |
| CONSUMO_ANULAR | ✓ | | |
| VENTA_REGISTRAR | ✓ | | ✓ |
| VENTA_ANULAR | ✓ | | |
| CAJA_OPERAR | ✓ | ✓ | ✓ |
| CAJA_SUPERVISAR | ✓ | ✓ | |
| MENU_GESTIONAR | ✓ | | |
| MENU_LEER | ✓ | ✓ | ✓ |
| PLAN_GESTIONAR | ✓ | ✓ | |
| CALENDARIO_GESTIONAR | ✓ | ✓ | |
| INVENTARIO_GESTIONAR | ✓ | ✓ | |
| GASTO_GESTIONAR | ✓ | ✓ | |
| REPORTE_VER | ✓ | ✓ | |

La identidad determina el local operativo a partir del empleado. Los filtros `localId` de otras sedes se permiten únicamente al administrador. Las lecturas, reportes y escrituras comprueban ese alcance. El dueño puede supervisar cajas de su local, pero únicamente el propietario de la caja o un administrador puede cerrarla.

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
- Cobrar exige venta pendiente, caja original abierta y pagos por el total exacto. Cobrar y anular solo se permiten al propietario de esa caja o a un ADMINISTRADOR; otro usuario recibe 403. Anular repone bebidas y devuelve cada pago si la venta estaba pagada.
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

Los tests de integración de negocio usan transacciones que revierten los datos de prueba. Como cualquier arranque, el contexto puede crear el primer administrador y actualizar estados vencidos mediante el bootstrap/scheduler. La conexión real valida todas las entidades; se prueban raciones, superposición, renovaciones, consumo doble, saldo, menú, caja, devoluciones, totales, compras, kardex, reportes, JWT, permisos, errores HTTP y paginación. SMTP requiere configuración externa y no se prueba con correos reales.

El test HTTP exporta `target/openapi.json`. [docs/openapi.json](docs/openapi.json) es una instantánea generada del contrato real. Tras cambiar endpoints, ejecutar la integración y luego `node tools/generar-api.mjs` para actualizar la documentación y la colección; Node se utiliza solo para esa generación, no para ejecutar la API.

Importar [postman/MANA.postman_collection.json](postman/MANA.postman_collection.json). Configurar `correo`, `clave`, ids y fechas. Login guarda `{{token}}`; la simulación guarda `montoContrato`; crear venta guarda `ventaId` y `montoVenta`. Las peticiones heredan Bearer. Los ejemplos no son datos iniciales adicionales ni una carga automática: ajustar importes según la simulación o el catálogo y ejecutar únicamente las operaciones elegidas. La colección contiene operaciones que crean, editan y anulan datos reales.

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
| GET | `/api/v1/consumos` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-026) |
| POST | `/api/v1/consumos` | CONSUMO_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-027) |
| PATCH | `/api/v1/consumos/{id}/anular` | CONSUMO_ANULAR | [Request / response](docs/ENDPOINTS.md#operacion-028) |
| GET | `/api/v1/contratos` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-029) |
| POST | `/api/v1/contratos` | CONTRATO_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-030) |
| GET | `/api/v1/contratos/{id}` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-031) |
| PATCH | `/api/v1/contratos/{id}/anular` | CONTRATO_ANULAR | [Request / response](docs/ENDPOINTS.md#operacion-032) |
| POST | `/api/v1/contratos/{id}/renovar` | CONTRATO_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-033) |
| POST | `/api/v1/contratos/simular` | CONTRATO_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-034) |
| GET | `/api/v1/dias-no-atencion` | CALENDARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-035) |
| POST | `/api/v1/dias-no-atencion` | CALENDARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-036) |
| PUT | `/api/v1/dias-no-atencion/{id}` | CALENDARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-037) |
| DELETE | `/api/v1/dias-no-atencion/{id}` | CALENDARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-038) |
| GET | `/api/v1/gastos` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-039) |
| POST | `/api/v1/gastos` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-040) |
| GET | `/api/v1/gastos/{id}` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-041) |
| PUT | `/api/v1/gastos/{id}` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-042) |
| DELETE | `/api/v1/gastos/{id}` | GASTO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-043) |
| GET | `/api/v1/locales` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-044) |
| POST | `/api/v1/locales` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-045) |
| GET | `/api/v1/locales/{id}` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-046) |
| PUT | `/api/v1/locales/{id}` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-047) |
| DELETE | `/api/v1/locales/{id}` | LOCAL_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-048) |
| GET | `/api/v1/menus` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-049) |
| PUT | `/api/v1/menus` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-050) |
| POST | `/api/v1/menus/clonar` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-051) |
| POST | `/api/v1/menus/copiar-a-cena` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-052) |
| GET | `/api/v1/movimientos-stock` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-053) |
| GET | `/api/v1/pensionistas/buscar` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-054) |
| GET | `/api/v1/planes` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-055) |
| POST | `/api/v1/planes` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-056) |
| GET | `/api/v1/planes/{id}` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-057) |
| PUT | `/api/v1/planes/{id}` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-058) |
| DELETE | `/api/v1/planes/{id}` | PLAN_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-059) |
| GET | `/api/v1/platos` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-060) |
| POST | `/api/v1/platos` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-061) |
| GET | `/api/v1/platos/{id}` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-062) |
| PUT | `/api/v1/platos/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-063) |
| DELETE | `/api/v1/platos/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-064) |
| GET | `/api/v1/pos/catalogo` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-065) |
| GET | `/api/v1/precios-sueltos` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-066) |
| POST | `/api/v1/precios-sueltos` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-067) |
| PUT | `/api/v1/precios-sueltos/{servicio}/{categoria}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-068) |
| DELETE | `/api/v1/precios-sueltos/{servicio}/{categoria}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-069) |
| GET | `/api/v1/proveedores` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-070) |
| POST | `/api/v1/proveedores` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-071) |
| GET | `/api/v1/proveedores/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-072) |
| PUT | `/api/v1/proveedores/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-073) |
| DELETE | `/api/v1/proveedores/{id}` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-074) |
| GET | `/api/v1/reglas-cobro` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-075) |
| POST | `/api/v1/reglas-cobro` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-076) |
| GET | `/api/v1/reglas-cobro/{id}` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-077) |
| PUT | `/api/v1/reglas-cobro/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-078) |
| DELETE | `/api/v1/reglas-cobro/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-079) |
| GET | `/api/v1/reportes/asistencia-diaria` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-080) |
| GET | `/api/v1/reportes/contratos-por-vencer` | CONTRATO_LEER | [Request / response](docs/ENDPOINTS.md#operacion-081) |
| GET | `/api/v1/reportes/gastos-vs-ingresos` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-082) |
| GET | `/api/v1/reportes/ingresos-mensuales` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-083) |
| GET | `/api/v1/reportes/platos-mas-consumidos` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-084) |
| GET | `/api/v1/reportes/ventas-vs-pensiones` | REPORTE_VER | [Request / response](docs/ENDPOINTS.md#operacion-085) |
| GET | `/api/v1/stock-bebidas` | INVENTARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-086) |
| GET | `/api/v1/tarifas` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-087) |
| POST | `/api/v1/tarifas` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-088) |
| GET | `/api/v1/tarifas/{id}` | MENU_LEER | [Request / response](docs/ENDPOINTS.md#operacion-089) |
| PUT | `/api/v1/tarifas/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-090) |
| DELETE | `/api/v1/tarifas/{id}` | MENU_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-091) |
| GET | `/api/v1/usuarios` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-092) |
| POST | `/api/v1/usuarios` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-093) |
| GET | `/api/v1/usuarios/{id}` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-094) |
| PUT | `/api/v1/usuarios/{id}` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-095) |
| DELETE | `/api/v1/usuarios/{id}` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-096) |
| PATCH | `/api/v1/usuarios/{id}/rol` | USUARIO_GESTIONAR | [Request / response](docs/ENDPOINTS.md#operacion-097) |
| GET | `/api/v1/ventas` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-098) |
| POST | `/api/v1/ventas` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-099) |
| GET | `/api/v1/ventas/{id}` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-100) |
| PATCH | `/api/v1/ventas/{id}/anular` | VENTA_ANULAR | [Request / response](docs/ENDPOINTS.md#operacion-101) |
| POST | `/api/v1/ventas/{id}/pagos` | VENTA_REGISTRAR | [Request / response](docs/ENDPOINTS.md#operacion-102) |
