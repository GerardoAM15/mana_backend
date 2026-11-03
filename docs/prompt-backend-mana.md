# Prompt para Codex — Backend MANÁ (Spring Boot + Oracle)

> Antes de pegar este prompt: copia `mana-hibrido-v2.sql` dentro del proyecto en `docs/mana-hibrido-v2.sql`.
> Codex lo usará como fuente de verdad del esquema.

---

Eres un desarrollador senior de Spring Boot. Vas a construir el backend del proyecto **MANÁ** (Modernización Administrativa en el Negocio de Alimentos) dentro del proyecto Spring Boot que ya está abierto en IntelliJ. Respeta el `groupId`, el paquete base y la versión de Spring Boot que ya tiene el proyecto.

## 1. Contexto del negocio

MANÁ es un comedor universitario con uno o varios locales. Tiene:

- **Pensionistas (solo prepago):** el cliente paga por adelantado un plan semanal o mensual y recibe raciones de desayuno (D), almuerzo (A) y/o cena (C). Cada día consume como máximo una ración por servicio. Si no viene, esa ración se pierde. El contrato termina en su fecha fin aunque le queden raciones.
- **Venta directa al público:** menú completo o platos sueltos, y bebidas del inventario, en un punto de venta con caja.
- **Planificación de menú:** banco de platos y menú programado por fecha y servicio.
- **Compras e inventario de bebidas**, proveedores y **gastos diarios**.
- **Caja:** apertura, cobros, cierre con arqueo por método de pago.

## 2. Base de datos (YA EXISTE — no la modifiques)

- Oracle en `localhost:1521`, service name `xepdb1`, usuario `MANADB`, contraseña mediante `DB_PASSWORD`.
- El esquema ya está creado y con datos iniciales. Su definición exacta está en **`docs/mana-hibrido-v2.sql`**. Léelo completo antes de escribir cualquier entidad.
- `spring.jpa.hibernate.ddl-auto: validate`. **Nunca** `update` ni `create`. No uses Flyway ni Liquibase (deshabilitados).
- Cada entidad JPA debe coincidir exactamente con su tabla: nombres de tabla y columna, tipos, longitudes, nulabilidad y `precision/scale`. El backend debe arrancar con `validate` sin errores.

### Configuración
- `application.yaml`: perfil activo por defecto `dev`, `open-in-view: false`, zona horaria `America/Lima` (`hibernate.jdbc.time_zone`), puerto `8080`, CORS para `http://localhost:4200`, Swagger en `/swagger-ui.html`.
- `application-dev.yaml`:
  ```yaml
  spring:
    datasource:
      url: ${DB_URL:jdbc:oracle:thin:@//localhost:1521/xepdb1}
      username: ${DB_USERNAME:MANADB}
      password: ${DB_PASSWORD}
  app:
    jwt:
      secret: ${JWT_SECRET:<clave base64 de 32+ bytes solo para desarrollo>}
  ```
- `application-prod.yaml`: todo por variables de entorno, sin valores por defecto.
- **El proyecto ya está creado**: `pom.xml` (Spring Boot 4.0.7, Java 21) con todas las dependencias (webmvc, data-jpa, validation, security, mail, ojdbc11, springdoc, jjwt, lombok, test), la clase `ManaBackendApplication` con `@EnableScheduling` y los tres `application*.yaml`. No cambies la versión de Spring Boot ni agregues Flyway. Si necesitas otra dependencia, agrégala y explica por qué.
- El primer administrador se configura en `app.admin.email`, `app.admin.password` y `app.admin.dni` (ya están en `application-dev.yaml`).

## 3. Arquitectura y estilo

### Arquitectura de referencia (obligatoria)
Replica la arquitectura del proyecto **AutoAndes**: misma estructura de paquetes, mismos nombres de clases de infraestructura y mismo estilo. Hay una copia en **`referencia/autoandes/`** dentro de este proyecto. Léela antes de empezar, sobre todo `config/`, `security/`, `exception/`, un servicio completo (`OrdenServiceImpl`) y su controller, DTOs y mapper. **Es solo de consulta:** no copies sus entidades ni su lógica de negocio, no la modifiques y no la incluyas en el build. Resumen de la estructura:

| Paquete | Contenido (mismo estilo que AutoAndes) |
|---|---|
| `config` | `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`, `CorsConfig`, `OpenApiConfig` (esquema Bearer en Swagger), `FechaConfig` (bean `Clock` con `app.zona-horaria`), `AuditoriaConfig` |
| `security` | `Permiso` (enum), `PermisosPorRol` (clase `final` con un `Map` estático rol → permisos), `UsuarioDetailsService`, `RestAuthenticationEntryPoint` (401 JSON), `RestAccessDeniedHandler` (403 JSON), `RespuestaErrorSeguridad` |
| `entity` | Entidades JPA con `@Getter @Setter` de Lombok (sin `@Data`) y enums de estado (`EstadoContrato`, `EstadoVenta`, ...) |
| `repository` | `JpaRepository` + `@Query` JPQL y métodos `buscarParaActualizar` con `@Lock(PESSIMISTIC_WRITE)` |
| `dto` | **Records** con sufijo `RequestDTO` / `ResponseDTO` (ej. `ContratoRequestDTO`, `ContratoResponseDTO`, `ErrorResponseDTO`, `LoginRequestDTO`, `LoginResponseDTO`), con Bean Validation en los de entrada |
| `mapper` | Clases `@Component` escritas a mano (`ContratoMapper.toResponse(...)`), **sin MapStruct** |
| `service` / `service/impl` | Interfaz `XxxService` + `XxxServiceImpl` con `@Service`, `@RequiredArgsConstructor` y `@Transactional(readOnly = true)` a nivel de clase |
| `controller` | `@RestController` + `@RequestMapping("/api/v1/...")`, `ResponseEntity.status(CREATED)` en los POST que crean |
| `exception` | `GlobalExceptionHandler`, `RecursoNoEncontradoException` (404), `ReglaNegocioException` (409), `DatosInvalidosException` (400) |
| `scheduler` | `ContratoScheduler` (único paquete nuevo respecto a AutoAndes) |

`ErrorResponseDTO` es igual al de AutoAndes: `record ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String mensaje, String ruta, Map<String,String> campos)` con `@JsonInclude(NON_NULL)`.

**Única diferencia con AutoAndes: aquí NO se usa Flyway.** La BD ya existe, así que no agregues `spring-boot-starter-flyway` ni carpetas de migración.

### Reglas de estilo
- Capas por paquete según la tabla de arriba.
- Inyección por constructor (`@RequiredArgsConstructor`), sin `@Autowired`.
- Nombres de métodos en español: `crear`, `actualizar`, `buscar`, `listar`, `eliminar`, `anular`, etc.
- Los controladores **nunca** devuelven entidades: siempre DTOs planos (con ids y nombres, sin entidades anidadas).
- `@Transactional(readOnly = true)` a nivel de clase en los servicios y `@Transactional` en los métodos que escriben.
- Todos los endpoints bajo `/api/v1`.
- Dinero siempre con `BigDecimal` (scale 2, `RoundingMode.HALF_UP`). Fechas con `LocalDate` / `LocalDateTime`; las columnas `timestamp with time zone` con `OffsetDateTime`.
- Inyecta un `Clock` (bean con zona `America/Lima`) y úsalo en todo cálculo de "hoy" y "ahora", para poder testear.

### Auditoría y bloqueo optimista
- Casi todas las tablas tienen `version`, `creado_en`, `actualizado_en`, `creado_por` y `actualizado_por`. Crea una `@MappedSuperclass` (`EntidadAuditable`) con `@Version` y JPA Auditing (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`), más un `AuditorAware` que devuelva el correo del usuario autenticado o `"sistema"`.
- Los catálogos y las tablas puente **no** tienen esas columnas. No heredes de la superclase en ellas. Tablas puente o de PK compuesta: `servicio_dias_atencion`, `plan_servicios`, `menu_platos`, `componentes_regla`, `precios_sueltos`, `proveedor_bebidas`, `detalle_combo_platos`, `correlativos`.
- `personas` → `clientes` / `empleados` con herencia `@Inheritance(strategy = JOINED)`.
- Los campos `activo`, `para_llevar`, `anulado` y `usado` son `NUMBER(1)`: mapéalos a `Boolean` con un `AttributeConverter`.
- Los estados (`ACTIVO`, `PAGADA`, etc.) son enums Java con `@Enumerated(STRING)`.

## 4. Seguridad

- JWT sin sesiones (stateless), BCrypt para las claves. Login por **correo + contraseña**.
- `POST /api/v1/auth/login` devuelve: `token`, `tipo`, `expiraEnSegundos`, `usuarioId`, `nombreCompleto`, `rol`, `localId`, `localNombre`, `permisos`.
- `GET /api/v1/auth/me` devuelve los mismos datos sin el token.
- El JWT lleva `sub` (correo) y `tv` (= `usuarios.token_version`). En cada request se vuelve a consultar el usuario: si está inactivo o `token_version` cambió, responde 401. Al cambiar la contraseña o el rol, incrementa `token_version`.
- Usuario inexistente, contraseña incorrecta y usuario inactivo devuelven el **mismo** 401.
- Sin token / token inválido → 401. Token válido sin permiso → 403. Ambos con el JSON de error estándar.
- **Primer administrador:** al arrancar, si no existe ningún usuario `ADMINISTRADOR`, créalo desde las variables `MANA_ADMIN_EMAIL`, `MANA_ADMIN_PASSWORD` y `MANA_ADMIN_DNI` (persona + empleado del local principal + usuario). Si faltan, registra un warning y no crees nada.
- **Restablecer contraseña (RF39–RF41):**
  - `POST /auth/recuperar` con el correo. Siempre responde 200, exista o no el correo.
  - Genera un token aleatorio de 32 bytes y guarda solo su hash SHA-256 en `tokens_restablecimiento`, con expiración de **5 minutos**.
  - Envía el enlace por correo con `JavaMailSender`. En el perfil dev, si no hay SMTP configurado, solo escribe el enlace en el log.
  - `POST /auth/restablecer` con `token` y `nuevaClave`: valida que el token no esté vencido ni usado, cambia la clave, lo marca como usado e incrementa `token_version`.

### Permisos
Centraliza los permisos en un enum `Permiso` y una clase `PermisosPorRol`. Protege las rutas en `SecurityConfig` con `hasAuthority`.

| Permiso | ADMINISTRADOR | DUENO | CAJERO |
|---|:-:|:-:|:-:|
| USUARIO_GESTIONAR, LOCAL_GESTIONAR | ✓ | | |
| CLIENTE_LEER | ✓ | ✓ | ✓ |
| CLIENTE_ESCRIBIR | ✓ | | ✓ |
| CONTRATO_LEER | ✓ | ✓ | ✓ |
| CONTRATO_REGISTRAR (registrar y renovar) | ✓ | | |
| CONTRATO_ANULAR | ✓ | | |
| CONSUMO_REGISTRAR | ✓ | | ✓ |
| CONSUMO_ANULAR | ✓ | | |
| VENTA_REGISTRAR (registrar y cobrar) | ✓ | | ✓ |
| VENTA_ANULAR | ✓ | | |
| CAJA_OPERAR (abrir, ver la propia, cerrar) | ✓ | ✓ | ✓ |
| CAJA_SUPERVISAR (ver todas) | ✓ | ✓ | |
| MENU_GESTIONAR (platos, programación, reglas, tarifas, precios sueltos) | ✓ | | |
| MENU_LEER | ✓ | ✓ | ✓ |
| PLAN_GESTIONAR, CALENDARIO_GESTIONAR | ✓ | ✓ | |
| INVENTARIO_GESTIONAR (bebidas, proveedores, compras) | ✓ | ✓ | |
| GASTO_GESTIONAR | ✓ | ✓ | |
| REPORTE_VER | ✓ | ✓ | |

**Multi-local:** el usuario opera siempre en el local de su empleado. Cualquier dato que el usuario lea o escriba (cajas, contratos, ventas, menú, stock, gastos) se filtra o valida con ese `localId`. El ADMINISTRADOR puede pasar `localId` como filtro opcional.

## 5. Reglas de negocio (implementarlas exactamente así)

### 5.1 Calendario y raciones
- Días de atención: tabla `servicio_dias_atencion` (ISO: 1 = lunes … 7 = domingo). D y A: lunes a viernes. C: lunes a jueves.
- `dias_no_atencion`: feriados o cierres por local. Esos días no cuentan como raciones y no se puede registrar consumo.
- **Fecha fin:** SEMANAL = `inicio + 6 días`. MENSUAL = `inicio.plusMonths(1).minusDays(1)`.
- **Raciones por servicio** = cantidad de días entre inicio y fin (inclusive) que sean día de atención del servicio y no estén en `dias_no_atencion` del local.
- Pon este cálculo en un componente propio, `CalculadoraRaciones`, con tests unitarios que verifiquen:
  - Semanal desde el lun 02/11/2026 → D 5, A 5, C 4.
  - Semanal desde el mié 04/11/2026 → D 5, A 5, C 4.
  - Mensual 02/11/2026–01/12/2026 → D 22, A 22, C 18.
  - Mensual 01/12/2026–31/12/2026 → D 23, A 23, C 19.
  - Con un feriado el 08/12/2026 → D 22, A 22, C 18.

### 5.2 Precios
| | Pensionista (`tarifas_servicio`) | Menú público (`reglas_cobro.precio_combo`) | Sueltos (`precios_sueltos`) | Recargo para llevar |
|---|---|---|---|---|
| D | 3.20 | 4.50 | pan 1.50, líquido 1.50 | 0 (menú y sueltos) |
| A | 10.00 | 11.00 | entrada 4.00, segundo 10.00 | menú 2.00, suelto 1.00 |
| C | 8.00 | 9.00 | no hay sueltos | menú 2.00 |

- Lee siempre los precios de la BD, nunca los dejes fijos en el código. La tabla es solo referencia para los tests.
- Composición del menú (`componentes_regla`): D = 1 BEBIDA_DESAYUNO + 2 PAN. A = ENTRADA + SEGUNDO + REFRESCO. C = SEGUNDO + REFRESCO.
- Las bebidas del inventario (`bebidas`) se venden aparte, al `precio_venta`, y **nunca** llevan recargo.

### 5.3 Contratos (solo prepago)
- `POST /contratos/simular` (sin guardar): recibe `clienteId`, `planId`, `fechaInicio` y `servicios[]`. Devuelve `fechaFin`, raciones, precio y subtotal por servicio, y el total.
- `POST /contratos`: los mismos datos más `pagos[]` (`{metodo, monto, referencia?}`). Todo en **una sola transacción**:
  1. El usuario debe tener una caja **abierta** en su local.
  2. El cliente debe estar activo, el plan activo, y los servicios deben pertenecer a `plan_servicios`.
  3. `fechaInicio >= hoy`.
  4. **Sin superposición:** el cliente no puede tener otro contrato `PROGRAMADO` o `ACTIVO` cuyo periodo se cruce con el nuevo (en cualquier local).
  5. Calcula las raciones (5.1) y copia el precio de `tarifas_servicio` a `precio_racion_pactado`. `subtotal = raciones × precio` y `monto_total` = suma de los subtotales.
  6. La suma de `pagos` debe ser **igual** a `monto_total`. Guarda cada pago en `pagos_contrato` (tipo COBRO, `caja_id` = caja abierta).
  7. El número sale de `correlativos` (tipo CONTRATO, `SELECT ... FOR UPDATE`) con formato `C-000001`.
  8. Estado: `ACTIVO` si `fechaInicio == hoy`; `PROGRAMADO` si es posterior.
- **Renovar** `POST /contratos/{id}/renovar`:
  - Solo un contrato `ACTIVO`, y como máximo **3 días antes** de su `fecha_fin`.
  - El nuevo contrato empieza el día siguiente al fin del anterior (`max(hoy, fecha_fin + 1)`) y apunta al anterior con `anterior_id`.
  - Puede cambiar el plan y los servicios. **No traslada raciones.** El contrato anterior sigue `ACTIVO` hasta su fin.
- **Anular** `PATCH /contratos/{id}/anular`:
  - Solo si **no tiene ningún consumo** (ni siquiera anulado) y el estado es PROGRAMADO o ACTIVO.
  - Pide un motivo de 10 a 250 caracteres.
  - Registra la devolución completa en `pagos_contrato` (tipo DEVOLUCION, mismos métodos y montos, caja abierta del usuario). El estado pasa a `ANULADO`.
- `GET /contratos?dni&estado&desde&hasta`, `GET /contratos/{id}` (con detalles, pagos y consumos), y `GET /clientes/{id}/contrato-vigente`.
- **Scheduler** `@Scheduled(cron = "0 5 0 * * *", zone = "America/Lima")`: pasa a FINALIZADO los contratos ACTIVO con `fecha_fin < hoy`, y a ACTIVO los PROGRAMADO con `fecha_inicio <= hoy`. Además, cada consulta de contratos debe mostrar el estado correcto aunque el scheduler aún no haya corrido. Una opción es ejecutar la misma actualización al iniciar la aplicación.

### 5.4 Consumos del pensionista (RF08–RF14)
- `GET /pensionistas/buscar?q=` busca por DNI o nombre en el local del usuario. Devuelve el contrato vigente, su estado, las fechas y el saldo por servicio (RF09).
- `POST /consumos` recibe `contratoId`, `servicio`, `platos[]` (`{platoId, cantidad}`), `paraLlevar` y `metodoRecargo?`.
  1. El contrato debe estar `ACTIVO`, ser del local del usuario, y hoy debe estar entre `fecha_inicio` y `fecha_fin`.
  2. Hoy tiene que ser día de atención del servicio y no estar en `dias_no_atencion`. Si no, devuelve 409 con un mensaje claro (por ejemplo: "La cena no se atiende los viernes").
  3. El contrato debe incluir ese servicio y su `saldo_raciones` debe ser mayor a 0. Bloquea el `detalle_contrato` con `PESSIMISTIC_WRITE`.
  4. Los platos tienen que estar en la **programación de hoy** del local para ese servicio, y la combinación tiene que cumplir **exactamente** `componentes_regla` de ese servicio.
  5. `clave_diaria = clienteId:fecha:servicio`. Si ya existe, devuelve 409 ("Ya consumió el almuerzo hoy").
  6. Resta 1 a `saldo_raciones`. `valor_racion = precio_racion_pactado`.
  7. Si `paraLlevar` y el servicio tiene `reglas_cobro.recargo_llevar > 0`, exige `metodoRecargo` y una caja abierta. Guarda `recargo_llevar`, `metodo_recargo` y `caja_id`. Si el recargo es 0 (desayuno), solo marca `para_llevar = 1`.
- `PATCH /consumos/{id}/anular` (RF14):
  - Solo consumos de **hoy** que no estén anulados. Pide un motivo.
  - Pone `anulado = 1`, `anulado_en`, `motivo_anulacion` y `clave_diaria = null`, y devuelve 1 ración al saldo.
  - Si tuvo recargo, solo se puede anular mientras esa caja siga abierta. La devolución se refleja porque los consumos anulados no cuentan en el cierre.

### 5.5 Ventas directas (RF15–RF19)
- `POST /ventas` recibe `servicio`, `paraLlevar`, `clienteId?` y `lineas[]`:
  - `PLATO {platoId, cantidad}`: el precio sale de `precios_sueltos` (servicio de la venta + categoría del plato). Si no hay fila activa, devuelve 409 ("En la cena solo se vende el menú completo").
  - `COMBO {reglaId, cantidad, platos[{platoId, cantidad}]}`: los platos deben cumplir `componentes_regla` para **una** unidad. Una línea con cantidad 2 lleva los mismos platos dos veces; si eligen platos distintos, van en líneas separadas.
  - `BEBIDA {bebidaId, cantidad}`: precio `bebidas.precio_venta`, recargo 0. Valida y descuenta `stock_bebidas` del local (con bloqueo) y registra `movimientos_stock` tipo VENTA con `detalle_orden_id`.
  - Los platos (sueltos y de combo) tienen que estar en la **programación de hoy** del local para ese servicio (RF28).
  - Precio y recargo se copian a `precio_unitario` y `recargo_unitario`. El recargo solo aplica si `paraLlevar`.
  - `recargo_llevar = Σ cantidad × recargo_unitario` y `total = Σ cantidad × precio_unitario + recargo_llevar`.
  - Requiere una caja abierta del usuario. `local_id` = local de la caja. Número `V-000001` sale de `correlativos` (tipo VENTA). Estado `PENDIENTE`.
  - Si en el mismo request vienen `pagos[]`, cobra de inmediato y la venta queda `PAGADA`.
- `POST /ventas/{id}/pagos` con `pagos[]` (RF17):
  - Solo ventas `PENDIENTE`, mientras su caja siga abierta.
  - Se permiten varios métodos, y la suma debe ser **igual** al total. Estado → `PAGADA`.
- `PATCH /ventas/{id}/anular` (RF18, RF19):
  - Solo si la **caja de la venta sigue abierta**. Pide un motivo.
  - Si estaba PAGADA, registra en `devoluciones_venta` una devolución por cada pago (mismo método y monto).
  - Devuelve el stock de las bebidas con un movimiento tipo DEVOLUCION. Estado → `ANULADA`.
- `GET /ventas?desde&hasta&estado&cajaId` y `GET /ventas/{id}` (con líneas, platos del combo, pagos y devoluciones).
- `GET /pos/catalogo?servicio=` (RF28): devuelve todo lo que el punto de venta puede vender hoy en ese servicio: el menú de hoy, la regla con su composición, los sueltos con precio y las bebidas con stock.

### 5.6 Caja
- `POST /cajas/abrir` con `montoInicial`. Una sola caja abierta por usuario: `clave_abierta = usuarioId`; si ya tiene una, devuelve 409.
- `GET /cajas/actual`: la caja abierta del usuario, o 404.
- `GET /cajas/{id}/resumen`: monto esperado por método de pago, calculado así:
  - EFECTIVO = `monto_inicial` + pagos de ventas en efectivo − devoluciones de ventas en efectivo + cobros de contratos en efectivo − devoluciones de contratos en efectivo + recargos de consumos **no anulados** en efectivo.
  - Cada uno de los otros métodos se calcula igual, pero sin `monto_inicial`.
  - Incluye también el conteo de ventas, consumos y contratos de la sesión.
- `POST /cajas/{id}/cerrar` con `[{metodo, montoContado}]` y `observacion?`:
  - Solo el dueño de la caja o un ADMINISTRADOR. Bloquea la sesión.
  - Si hay ventas `PENDIENTE` en esa caja, rechaza el cierre (409).
  - Calcula lo esperado, guarda `detalles_cierre_caja` (una fila por método con movimiento o con monto contado mayor a 0), y totaliza `monto_esperado`, `monto_contado` y `diferencia`.
  - Pone `cierre = ahora` y `clave_abierta = null`.
- `GET /cajas?desde&hasta&usuarioId` (CAJA_SUPERVISAR).

### 5.7 Menú y catálogos
- Platos (CU_11, RF25): CRUD. "Eliminar" es baja lógica si el plato está usado en menús, ventas o consumos.
- Programación (CU_12, RF26–RF27):
  - `PUT /menus` con `{fecha, servicio, platoIds[]}` crea o reemplaza el menú de esa fecha, servicio y local.
  - `GET /menus?desde&hasta&servicio`.
  - `POST /menus/clonar` con `{fechaOrigen, fechaDestino, servicios[]?}` (RF27).
  - `POST /menus/copiar-a-cena` con `{fecha}`: copia los **segundos** del almuerzo de esa fecha al menú de la cena del mismo día, sumándolos a lo que ya tenga.
- Reglas de cobro (CU_13), tarifas (CU_14), precios sueltos: CRUD con validaciones de montos.
  - **Cambiar una tarifa no afecta contratos existentes**, porque el precio quedó congelado en cada contrato.
- Tipos de plan (CU_22) con sus servicios, y `dias_no_atencion`: CRUD.
- Clientes (CU_01): persona + cliente. DNI de 8 dígitos y único (409 si se repite).
- Usuarios (CU_18): crear persona + empleado + usuario en un solo paso; editar; cambiar rol; desactivar. Nunca devuelvas `clave_hash`.
- Catálogos de solo lectura: `GET /catalogos` (servicios, métodos de pago, categorías, roles, días de atención).

### 5.8 Inventario y gastos
- Proveedores con sus bebidas (CU_21) y bebidas: CRUD.
- `POST /compras-bebidas` (CU_16): recibe `proveedorId`, `fecha` y `detalles[]` (`{bebidaId, presentaciones, unidadesPorPresentacion, costoTotal}`).
  - Cada bebida debe ser una de las que vende ese proveedor.
  - Suma `presentaciones × unidadesPorPresentacion` al `stock_bebidas` del local. Si no existe la fila de stock, créala.
  - Registra un movimiento tipo COMPRA. `total` = suma de `costoTotal`.
- `GET /stock-bebidas` y `GET /movimientos-stock?bebidaId&desde&hasta` (kardex).
- Gastos diarios (CU_15): crear, listar por rango, editar y desactivar.

### 5.9 Reportes (RF46–RF51, REPORTE_VER)
Usa consultas JPQL o nativas que devuelvan DTOs (nada de cargar entidades para sumar). Todos los reportes reciben `desde`/`hasta` y opcionalmente `localId`:
1. `GET /reportes/ingresos-mensuales`: cobros netos de contratos y ventas pagadas netas por mes.
2. `GET /reportes/platos-mas-consumidos?servicio=`: top de platos en consumos no anulados, por servicio.
3. `GET /reportes/contratos-por-vencer?dias=3`: contratos activos que vencen pronto, con su saldo (reemplaza el reporte de morosidad).
4. `GET /reportes/ventas-vs-pensiones`: comparativo de ingresos.
5. `GET /reportes/asistencia-diaria`: consumos por día y servicio.
6. `GET /reportes/gastos-vs-ingresos`: por día o por mes.

## 6. Errores y validación
- `@RestControllerAdvice` con un JSON único: `{timestamp, status, error, mensaje, ruta, campos?}`.
- Códigos:
  - 400: validación, tipos inválidos o JSON mal formado.
  - 401: falta autenticación o el token no sirve.
  - 403: sin permiso.
  - 404: no existe el recurso.
  - 409: regla de negocio, duplicado, FK en uso, `OptimisticLock` o `PessimisticLock` ("Otro usuario modificó el registro; vuelva a consultar").
  - 500: cualquier otro error, sin exponer detalles internos.
- Bean Validation en todos los DTO de entrada, con mensajes en español.
- Logs `INFO` en operaciones de dinero (contrato, venta, pago, anulación, cierre), con número y usuario. Nunca registres contraseñas ni tokens en el log.

## 7. Concurrencia
Usa `@Lock(PESSIMISTIC_WRITE)` en:
- `correlativos`, al generar números;
- `detalles_contrato`, al consumir o anular;
- `stock_bebidas`, al vender, comprar o devolver;
- `sesiones_caja`, al cobrar, anular o cerrar (para que no se cobre en una caja que se está cerrando);
- el cliente, al crear o renovar un contrato (para evitar dos contratos superpuestos creados al mismo tiempo).

Bloquea siempre en orden estable (por id ascendente) para evitar deadlocks.

## 8. Tests
- Unitarios: `CalculadoraRaciones` (los casos de 5.1), el cálculo de totales y recargos de una venta (por ejemplo, 2 menús de almuerzo para llevar = 26.00; 1 almuerzo + 1 entrada para llevar = 18.00; 1 desayuno para llevar = 4.50), y `PermisosPorRol`.
- Unitarios con Mockito para las reglas de contrato, consumo y venta: superposición, saldo 0, cena en viernes, consumo doble en el día, cena sin sueltos, caja cerrada.
- Un test `@SpringBootTest` que arranque contra la BD real con `ddl-auto=validate`. Márcalo para que solo corra con un perfil o variable, así no rompe el build si no hay Oracle.

## 9. Entregables
- Código completo que compile con `./mvnw clean package`.
- `README.md` en español con:
  - cómo ejecutar el proyecto;
  - variables de entorno;
  - cómo crear el primer administrador;
  - la tabla de permisos;
  - la lista de endpoints con método, ruta, permiso y un ejemplo de request y response;
  - las reglas de negocio resumidas.
- Colección Postman en `postman/` con login y una variable `{{token}}`.

## 10. Forma de trabajo
Hazlo **por fases** y confirma que compila y arranca al final de cada una antes de seguir:
1. Configuración, conexión, todas las entidades y repositorios. **El backend debe arrancar con `validate` sin errores.**
2. Seguridad: login, JWT, permisos, primer administrador, restablecer contraseña.
3. Catálogos, clientes, usuarios, planes, calendario, platos, menú, reglas, tarifas y precios.
4. Caja.
5. Contratos y consumos.
6. Ventas directas.
7. Inventario y gastos.
8. Reportes, tests, README y Postman.

Si encuentras una contradicción entre este prompt y `docs/mana-hibrido-v2.sql`, **manda el SQL** (la BD ya existe). Detente y avísame en vez de cambiar la base de datos.
