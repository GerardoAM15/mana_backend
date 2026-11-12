-- =====================================================================
-- MANÁ: esquema físico híbrido y multi-local (Oracle 12c o superior)
-- Versión 4 — solo prepago, calendario de atención, caja con arqueo,
-- precios por servicio, recargo para llevar y jerarquía de roles.
-- Cambios respecto a la v3:
--   * Una renovación ANULADA ya no bloquea volver a renovar el contrato:
--     la UNIQUE de contratos.anterior_id pasa a un índice único que ignora
--     los contratos anulados (uq_contratos_anterior).
--   * Índice único uq_reglas_cobro_activa: un solo menú público activo
--     por servicio (RF29), también protegido en la BD.
--   * Permisos READ de solo lectura para ROL_MANA_CONSULTA sobre una lista
--     explícita de tablas, más las vistas v_personas (sin DNI ni teléfono) y
--     v_usuarios (sin correo ni clave_hash). Ese rol no ve contraseñas,
--     tokens de restablecimiento ni datos de contacto (sección 13).
--   * READ exige Oracle 12.1.0.2 o superior (XE 18c/21c lo incluyen).
--   * Columnas y tipos idénticos a la v3: el backend (ddl-auto: validate)
--     funciona sin cambios.
-- Orden de instalación:
--   1. mana-usuarios-roles.sql  como SYSTEM (crea MANADB, MANA_CONSULTA y el rol)
--   2. mana-hibrido-v4.sql      como MANADB, en un esquema vacío
--   3. mana-datos-ejemplo.sql   como MANADB (opcional, solo desarrollo)
--   Para vaciar el esquema antes de reinstalar: mana-drop.sql como MANADB.
-- ---------------------------------------------------------------------
-- Reglas de negocio que soporta el esquema:
--   * Pensión SOLO PREPAGO. El contrato se paga completo al crearlo
--     (uno o varios pagos que suman monto_total).
--   * Un contrato tiene una sola fecha_inicio / fecha_fin para todos sus
--     servicios. Semanal: inicio + 6 días. Mensual: inicio + 1 mes - 1.
--   * Las raciones se calculan con el calendario (servicio_dias_atencion
--     menos dias_no_atencion). D y A: lunes a viernes. C: lunes a jueves.
--   * Si el pensionista no viene, la ración se pierde. Al llegar fecha_fin
--     el contrato FINALIZA aunque tenga saldo. No hay traslado de raciones.
--   * Máximo una ración por servicio por día (consumos.clave_diaria).
--   * Un cliente no puede tener contratos con periodos superpuestos
--     (lo valida el backend). Renovar: hasta 3 días antes del fin.
--   * El contrato solo vale en el local donde se hizo.
--   * Anular contrato: solo si no tiene consumos; devolución completa.
--   * Precios: pensionista en tarifas_servicio; menú público en
--     reglas_cobro; sueltos en precios_sueltos (la cena no tiene sueltos).
--   * Para llevar: recargo por menú (D 0, A 2, C 2) y por suelto
--     (A 1; desayuno 0). Las bebidas del inventario no tienen recargo.
--   * Todo cobro pasa por una sesión de caja abierta (ventas, pagos de
--     contrato y recargos de pensionista) y entra en su cierre.
--   * El esperado de caja puede ser negativo por devoluciones, incluso por
--     método. El monto contado nunca es negativo. Una devolución en EFECTIVO
--     solo se permite si el efectivo esperado de la caja la cubre (backend).
--   * Roles: DUENO (todo) > ADMINISTRADOR (todo menos locales; solo gestiona
--     cajeros) > CAJERO. Siempre debe existir al menos un dueño activo.
--
-- Uso: ejecutar en un esquema vacío con SQL*Plus, SQLcl o SQL Developer.
-- No contiene credenciales. Si no existe ningún DUENO, el backend crea el
-- usuario inicial (rol DUENO) con MANA_ADMIN_EMAIL, MANA_ADMIN_PASSWORD y
-- MANA_ADMIN_DNI. Para datos de prueba ejecute después mana-datos-ejemplo.sql.
--
-- Columnas de auditoría (todas las tablas salvo catálogos y tablas
-- puente): version (bloqueo optimista), creado_en, actualizado_en,
-- creado_por y actualizado_por.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Catálogos
-- ---------------------------------------------------------------------
create table roles (
    codigo          varchar2(32 char)  not null,
    nombre          varchar2(60 char)  not null,
    descripcion     varchar2(250 char),
    activo          number(1,0)        default 1 not null,
    constraint pk_roles primary key (codigo),
    constraint ck_roles_activo check (activo in (0,1))
);

create table metodos_pago (
    codigo          varchar2(32 char)  not null,
    nombre          varchar2(60 char)  not null,
    descripcion     varchar2(250 char),
    activo          number(1,0)        default 1 not null,
    constraint pk_metodos_pago primary key (codigo),
    constraint ck_metodos_pago_activo check (activo in (0,1))
);

create table categorias_plato (
    codigo          varchar2(32 char)  not null,
    nombre          varchar2(60 char)  not null,
    descripcion     varchar2(250 char),
    activo          number(1,0)        default 1 not null,
    constraint pk_categorias_plato primary key (codigo),
    constraint ck_categorias_plato_activo check (activo in (0,1))
);

-- "Turno" del modelo lógico: D desayuno, A almuerzo, C cena.
create table servicios (
    codigo          varchar2(32 char)  not null,
    nombre          varchar2(60 char)  not null,
    descripcion     varchar2(250 char),
    activo          number(1,0)        default 1 not null,
    constraint pk_servicios primary key (codigo),
    constraint ck_servicios_activo check (activo in (0,1))
);

-- Días de la semana en que se atiende cada servicio (ISO: 1 lunes ... 7 domingo).
create table servicio_dias_atencion (
    servicio        varchar2(32 char)  not null,
    dia_semana      number(1,0)        not null,
    constraint pk_servicio_dias_atencion primary key (servicio, dia_semana),
    constraint fk_servicio_dias_servicio foreign key (servicio) references servicios (codigo),
    constraint ck_servicio_dias_semana check (dia_semana between 1 and 7)
);

-- ---------------------------------------------------------------------
-- 2. Locales, calendario y correlativos
-- ---------------------------------------------------------------------
create table locales (
    id              number(19,0) generated by default as identity,
    nombre          varchar2(120 char) not null,
    direccion       varchar2(250 char),
    telefono        varchar2(20 char),
    activo          number(1,0)        default 1 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_locales primary key (id),
    constraint ck_locales_activo check (activo in (0,1))
);

-- Feriados o cierres: ese día no se atiende y no cuenta como ración.
create table dias_no_atencion (
    id              number(19,0) generated by default as identity,
    local_id        number(19,0)       not null,
    fecha           date               not null,
    motivo          varchar2(160 char) not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_dias_no_atencion primary key (id),
    constraint uq_dias_no_atencion unique (local_id, fecha),
    constraint fk_dias_no_atencion_local foreign key (local_id) references locales (id)
);

-- Último número usado por local y tipo (V-000123, C-000045).
-- El backend lo lee con SELECT ... FOR UPDATE y lo incrementa en la misma transacción.
create table correlativos (
    local_id        number(19,0)       not null,
    tipo            varchar2(16 char)  not null,
    ultimo_numero   number(10,0)       default 0 not null,
    constraint pk_correlativos primary key (local_id, tipo),
    constraint fk_correlativos_local foreign key (local_id) references locales (id),
    constraint ck_correlativos_tipo check (tipo in ('VENTA','CONTRATO')),
    constraint ck_correlativos_numero check (ultimo_numero >= 0)
);

-- ---------------------------------------------------------------------
-- 3. Personas, clientes, empleados y seguridad
-- ---------------------------------------------------------------------
create table personas (
    id              number(19,0) generated by default as identity,
    dni             varchar2(8 char)   not null,
    nombre          varchar2(100 char) not null,
    apellido        varchar2(100 char) not null,
    telefono        varchar2(20 char),
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_personas primary key (id),
    constraint uq_personas_dni unique (dni),
    constraint ck_personas_dni check (regexp_like(dni, '^[0-9]{8}$'))
);

-- Especialización de Persona (herencia JOINED en JPA).
create table clientes (
    id              number(19,0)       not null,
    activo          number(1,0)        default 1 not null,
    constraint pk_clientes primary key (id),
    constraint fk_clientes_persona foreign key (id) references personas (id),
    constraint ck_clientes_activo check (activo in (0,1))
);

create table empleados (
    id              number(19,0)       not null,
    local_id        number(19,0)       not null,
    cargo           varchar2(100 char) not null,
    activo          number(1,0)        default 1 not null,
    constraint pk_empleados primary key (id),
    constraint fk_empleados_persona foreign key (id) references personas (id),
    constraint fk_empleados_local foreign key (local_id) references locales (id),
    constraint ck_empleados_activo check (activo in (0,1))
);

create table usuarios (
    id              number(19,0) generated by default as identity,
    empleado_id     number(19,0)       not null,
    correo          varchar2(160 char) not null,
    clave_hash      varchar2(100 char) not null,
    rol             varchar2(32 char)  not null,
    activo          number(1,0)        default 1 not null,
    token_version   number(10,0)       default 0 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_usuarios primary key (id),
    constraint uq_usuarios_correo unique (correo),
    constraint uq_usuarios_empleado unique (empleado_id),
    constraint fk_usuarios_empleado foreign key (empleado_id) references empleados (id),
    constraint fk_usuarios_rol foreign key (rol) references roles (codigo),
    constraint ck_usuarios_activo check (activo in (0,1))
);

-- RF39-RF41: token de un solo uso con vencimiento de 5 minutos (lo fija el backend).
create table tokens_restablecimiento (
    id              number(19,0) generated by default as identity,
    usuario_id      number(19,0)       not null,
    token_hash      varchar2(64 char)  not null,
    expira_en       timestamp(9) with time zone not null,
    usado           number(1,0)        default 0 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_tokens_restablecimiento primary key (id),
    constraint uq_tokens_hash unique (token_hash),
    constraint fk_tokens_usuario foreign key (usuario_id) references usuarios (id),
    constraint ck_tokens_usado check (usado in (0,1))
);

-- ---------------------------------------------------------------------
-- 4. Planes, precios, platos y menú
-- ---------------------------------------------------------------------
-- El plan define el periodo; las raciones se calculan con el calendario.
create table tipos_plan (
    id              number(19,0) generated by default as identity,
    nombre          varchar2(120 char) not null,
    periodo         varchar2(10 char)  not null,
    activo          number(1,0)        default 1 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_tipos_plan primary key (id),
    constraint uq_tipos_plan_nombre unique (nombre),
    constraint ck_tipos_plan_periodo check (periodo in ('SEMANAL','MENSUAL')),
    constraint ck_tipos_plan_activo check (activo in (0,1))
);

-- Servicios que el plan ofrece; el cliente elige cuáles contrata.
create table plan_servicios (
    plan_id         number(19,0)       not null,
    servicio        varchar2(32 char)  not null,
    constraint pk_plan_servicios primary key (plan_id, servicio),
    constraint fk_plan_servicios_plan foreign key (plan_id) references tipos_plan (id),
    constraint fk_plan_servicios_servicio foreign key (servicio) references servicios (codigo)
);

-- CU_14: precio por ración para pensionistas (D 3.20, A 10.00, C 8.00).
create table tarifas_servicio (
    id              number(19,0) generated by default as identity,
    servicio        varchar2(32 char)  not null,
    precio          number(14,2)       not null,
    activo          number(1,0)        default 1 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_tarifas_servicio primary key (id),
    constraint uq_tarifas_servicio unique (servicio),
    constraint fk_tarifas_servicio foreign key (servicio) references servicios (codigo),
    constraint ck_tarifas_precio check (precio > 0),
    constraint ck_tarifas_activo check (activo in (0,1))
);

create table platos (
    id              number(19,0) generated by default as identity,
    nombre          varchar2(120 char) not null,
    categoria       varchar2(32 char)  not null,
    activo          number(1,0)        default 1 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_platos primary key (id),
    constraint fk_platos_categoria foreign key (categoria) references categorias_plato (codigo),
    constraint ck_platos_activo check (activo in (0,1))
);

create table programaciones_menu (
    id              number(19,0) generated by default as identity,
    local_id        number(19,0)       not null,
    fecha           date               not null,
    servicio        varchar2(32 char)  not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_programaciones_menu primary key (id),
    constraint uq_menu_local_fecha_servicio unique (local_id, fecha, servicio),
    constraint fk_menu_local foreign key (local_id) references locales (id),
    constraint fk_menu_servicio foreign key (servicio) references servicios (codigo)
);

create table menu_platos (
    menu_id         number(19,0)       not null,
    plato_id        number(19,0)       not null,
    constraint pk_menu_platos primary key (menu_id, plato_id),
    constraint fk_menu_platos_menu foreign key (menu_id) references programaciones_menu (id),
    constraint fk_menu_platos_plato foreign key (plato_id) references platos (id)
);

-- CU_13: menú completo al público, con su composición y recargo para llevar.
create table reglas_cobro (
    id              number(19,0) generated by default as identity,
    nombre          varchar2(120 char) not null,
    servicio        varchar2(32 char)  not null,
    precio_combo    number(14,2)       not null,
    recargo_llevar  number(14,2)       default 0 not null,
    activo          number(1,0)        default 1 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_reglas_cobro primary key (id),
    constraint uq_reglas_cobro_nombre unique (nombre),
    constraint fk_reglas_cobro_servicio foreign key (servicio) references servicios (codigo),
    constraint ck_reglas_cobro_montos check (precio_combo > 0 and recargo_llevar >= 0),
    constraint ck_reglas_cobro_activo check (activo in (0,1))
);

-- También valida qué elige el pensionista en cada consumo.
create table componentes_regla (
    regla_id        number(19,0)       not null,
    categoria       varchar2(32 char)  not null,
    cantidad        number(10,0)       not null,
    constraint pk_componentes_regla primary key (regla_id, categoria),
    constraint fk_componentes_regla foreign key (regla_id) references reglas_cobro (id),
    constraint fk_componentes_categoria foreign key (categoria) references categorias_plato (codigo),
    constraint ck_componentes_cantidad check (cantidad > 0)
);

-- Precio suelto al público por servicio y categoría. La cena no tiene filas.
create table precios_sueltos (
    servicio        varchar2(32 char)  not null,
    categoria       varchar2(32 char)  not null,
    precio          number(14,2)       not null,
    recargo_llevar  number(14,2)       default 0 not null,
    activo          number(1,0)        default 1 not null,
    constraint pk_precios_sueltos primary key (servicio, categoria),
    constraint fk_precios_sueltos_servicio foreign key (servicio) references servicios (codigo),
    constraint fk_precios_sueltos_categoria foreign key (categoria) references categorias_plato (codigo),
    constraint ck_precios_sueltos check (precio > 0 and recargo_llevar >= 0),
    constraint ck_precios_sueltos_activo check (activo in (0,1))
);

-- ---------------------------------------------------------------------
-- 5. Caja (antes que contratos y ventas porque todo cobro la referencia)
-- ---------------------------------------------------------------------
create table sesiones_caja (
    id              number(19,0) generated by default as identity,
    local_id        number(19,0)       not null,
    usuario_id      number(19,0)       not null,
    apertura        timestamp(9)       not null,
    cierre          timestamp(9),
    monto_inicial   number(14,2)       not null,
    monto_esperado  number(14,2),
    monto_contado   number(14,2),
    diferencia      number(14,2),
    observacion     varchar2(250 char),
    clave_abierta   varchar2(80 char),
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_sesiones_caja primary key (id),
    constraint uq_cajas_clave_abierta unique (clave_abierta),
    constraint fk_cajas_local foreign key (local_id) references locales (id),
    constraint fk_cajas_usuario foreign key (usuario_id) references usuarios (id),
    constraint ck_cajas_monto_inicial check (monto_inicial >= 0),
    constraint ck_cajas_estado check (
        (cierre is null and clave_abierta is not null
            and monto_esperado is null and monto_contado is null and diferencia is null) or
        (cierre is not null and clave_abierta is null and cierre >= apertura
            and monto_esperado is not null and monto_contado >= 0 and diferencia is not null))
);

-- Arqueo: una fila por método de pago al cerrar la caja.
create table detalles_cierre_caja (
    id              number(19,0) generated by default as identity,
    sesion_caja_id  number(19,0)       not null,
    metodo          varchar2(32 char)  not null,
    monto_esperado  number(14,2)       not null,
    monto_contado   number(14,2)       not null,
    diferencia      number(14,2)       not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_detalles_cierre_caja primary key (id),
    constraint uq_cierre_caja_metodo unique (sesion_caja_id, metodo),
    constraint fk_cierre_caja_sesion foreign key (sesion_caja_id) references sesiones_caja (id),
    constraint fk_cierre_caja_metodo foreign key (metodo) references metodos_pago (codigo),
    -- monto_esperado puede ser negativo: p. ej. devolución por Yape de un contrato
    -- cobrado en otra caja. Lo contado nunca es negativo.
    constraint ck_cierre_caja_montos check (
        monto_contado >= 0 and diferencia = monto_contado - monto_esperado)
);

-- ---------------------------------------------------------------------
-- 6. Pensiones (solo prepago): contratos, consumos y pagos
-- ---------------------------------------------------------------------
create table contratos (
    id               number(19,0) generated by default as identity,
    numero           varchar2(12 char)  not null,
    local_id         number(19,0)       not null,
    cliente_id       number(19,0)       not null,
    plan_id          number(19,0)       not null,
    estado           varchar2(16 char)  not null,
    fecha_inicio     date               not null,
    fecha_fin        date               not null,
    monto_total      number(14,2)       not null,
    anterior_id      number(19,0),
    anulado_en       timestamp(9),
    motivo_anulacion varchar2(250 char),
    version          number(19,0)       default 0 not null,
    creado_en        timestamp(9) with time zone not null,
    actualizado_en   timestamp(9) with time zone not null,
    creado_por       varchar2(160 char) not null,
    actualizado_por  varchar2(160 char) not null,
    constraint pk_contratos primary key (id),
    constraint uq_contratos_numero unique (local_id, numero),
    -- anterior_id es único solo entre contratos no anulados: ver uq_contratos_anterior (sección 10).
    constraint fk_contratos_local foreign key (local_id) references locales (id),
    constraint fk_contratos_cliente foreign key (cliente_id) references clientes (id),
    constraint fk_contratos_plan foreign key (plan_id) references tipos_plan (id),
    constraint fk_contratos_anterior foreign key (anterior_id) references contratos (id),
    constraint ck_contratos_estado check (estado in ('PROGRAMADO','ACTIVO','FINALIZADO','ANULADO')),
    constraint ck_contratos_fechas check (fecha_fin >= fecha_inicio),
    constraint ck_contratos_total check (monto_total > 0),
    constraint ck_contratos_anulacion check (
        (estado = 'ANULADO' and anulado_en is not null and motivo_anulacion is not null) or
        (estado <> 'ANULADO' and anulado_en is null and motivo_anulacion is null))
);

-- Servicios contratados: raciones calculadas por calendario y precio congelado.
create table detalles_contrato (
    id                    number(19,0) generated by default as identity,
    contrato_id           number(19,0)       not null,
    servicio              varchar2(32 char)  not null,
    raciones_contratadas  number(10,0)       not null,
    saldo_raciones        number(10,0)       not null,
    precio_racion_pactado number(14,2)       not null,
    subtotal              number(14,2)       not null,
    version               number(19,0)       default 0 not null,
    creado_en             timestamp(9) with time zone not null,
    actualizado_en        timestamp(9) with time zone not null,
    creado_por            varchar2(160 char) not null,
    actualizado_por       varchar2(160 char) not null,
    constraint pk_detalles_contrato primary key (id),
    constraint uq_detalles_contrato unique (contrato_id, servicio),
    constraint fk_detalles_contrato foreign key (contrato_id) references contratos (id),
    constraint fk_detalles_contrato_servicio foreign key (servicio) references servicios (codigo),
    constraint ck_detalles_contrato_raciones check (
        raciones_contratadas > 0 and saldo_raciones between 0 and raciones_contratadas),
    constraint ck_detalles_contrato_montos check (
        precio_racion_pactado > 0 and subtotal = raciones_contratadas * precio_racion_pactado)
);

-- Control_Cliente del modelo lógico: una ración consumida.
create table consumos (
    id                  number(19,0) generated by default as identity,
    detalle_contrato_id number(19,0)       not null,
    fecha               date               not null,
    registrado_en       timestamp(9)       not null,
    valor_racion        number(14,2)       not null,
    clave_diaria        varchar2(100 char),
    para_llevar         number(1,0)        default 0 not null,
    recargo_llevar      number(14,2)       default 0 not null,
    metodo_recargo      varchar2(32 char),
    caja_id             number(19,0),
    anulado             number(1,0)        default 0 not null,
    anulado_en          timestamp(9),
    motivo_anulacion    varchar2(250 char),
    version             number(19,0)       default 0 not null,
    creado_en           timestamp(9) with time zone not null,
    actualizado_en      timestamp(9) with time zone not null,
    creado_por          varchar2(160 char) not null,
    actualizado_por     varchar2(160 char) not null,
    constraint pk_consumos primary key (id),
    constraint uq_consumos_clave_diaria unique (clave_diaria),
    constraint fk_consumos_detalle foreign key (detalle_contrato_id) references detalles_contrato (id),
    constraint fk_consumos_metodo foreign key (metodo_recargo) references metodos_pago (codigo),
    constraint fk_consumos_caja foreign key (caja_id) references sesiones_caja (id),
    constraint ck_consumos_valor check (valor_racion > 0),
    constraint ck_consumos_llevar check (para_llevar in (0,1)),
    constraint ck_consumos_recargo check (
        (recargo_llevar = 0 and metodo_recargo is null) or
        (para_llevar = 1 and recargo_llevar > 0 and metodo_recargo is not null and caja_id is not null)),
    constraint ck_consumos_anulado check (
        (anulado = 0 and anulado_en is null and motivo_anulacion is null and clave_diaria is not null) or
        (anulado = 1 and anulado_en is not null and motivo_anulacion is not null and clave_diaria is null))
);

create table detalles_consumo (
    id                  number(19,0) generated by default as identity,
    consumo_id          number(19,0)       not null,
    plato_id            number(19,0)       not null,
    categoria_consumida varchar2(32 char)  not null,
    cantidad            number(10,0)       default 1 not null,
    version             number(19,0)       default 0 not null,
    creado_en           timestamp(9) with time zone not null,
    actualizado_en      timestamp(9) with time zone not null,
    creado_por          varchar2(160 char) not null,
    actualizado_por     varchar2(160 char) not null,
    constraint pk_detalles_consumo primary key (id),
    constraint uq_detalles_consumo unique (consumo_id, plato_id),
    constraint fk_detalles_consumo foreign key (consumo_id) references consumos (id),
    constraint fk_detalles_consumo_plato foreign key (plato_id) references platos (id),
    constraint fk_detalles_consumo_categoria foreign key (categoria_consumida)
        references categorias_plato (codigo),
    constraint ck_detalles_consumo_cantidad check (cantidad > 0)
);

-- Pagos del contrato (pago adelantado, uno o varios métodos) y devoluciones.
create table pagos_contrato (
    id              number(19,0) generated by default as identity,
    contrato_id     number(19,0)       not null,
    caja_id         number(19,0)       not null,
    tipo            varchar2(16 char)  not null,
    monto           number(14,2)       not null,
    metodo          varchar2(32 char)  not null,
    fecha           timestamp(9)       not null,
    referencia      varchar2(160 char),
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_pagos_contrato primary key (id),
    constraint fk_pagos_contrato_contrato foreign key (contrato_id) references contratos (id),
    constraint fk_pagos_contrato_caja foreign key (caja_id) references sesiones_caja (id),
    constraint fk_pagos_contrato_metodo foreign key (metodo) references metodos_pago (codigo),
    constraint ck_pagos_contrato_tipo check (tipo in ('COBRO','DEVOLUCION')),
    constraint ck_pagos_contrato_monto check (monto > 0)
);

-- ---------------------------------------------------------------------
-- 7. Bebidas, proveedores e inventario por local
-- ---------------------------------------------------------------------
-- Bebidas del inventario (gaseosas, agua...): se venden aparte y sin recargo.
create table bebidas (
    id              number(19,0) generated by default as identity,
    nombre          varchar2(120 char) not null,
    precio_venta    number(14,2)       not null,
    activo          number(1,0)        default 1 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_bebidas primary key (id),
    constraint ck_bebidas_precio check (precio_venta > 0),
    constraint ck_bebidas_activo check (activo in (0,1))
);

create table stock_bebidas (
    id              number(19,0) generated by default as identity,
    local_id        number(19,0)       not null,
    bebida_id       number(19,0)       not null,
    stock           number(10,0)       default 0 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_stock_bebidas primary key (id),
    constraint uq_stock_local_bebida unique (local_id, bebida_id),
    constraint fk_stock_local foreign key (local_id) references locales (id),
    constraint fk_stock_bebida foreign key (bebida_id) references bebidas (id),
    constraint ck_stock_no_negativo check (stock >= 0)
);

create table proveedores (
    id              number(19,0) generated by default as identity,
    razon_social    varchar2(160 char) not null,
    telefono        varchar2(20 char),
    activo          number(1,0)        default 1 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_proveedores primary key (id),
    constraint ck_proveedores_activo check (activo in (0,1))
);

create table proveedor_bebidas (
    proveedor_id    number(19,0)       not null,
    bebida_id       number(19,0)       not null,
    constraint pk_proveedor_bebidas primary key (bebida_id, proveedor_id),
    constraint fk_proveedor_bebidas_prov foreign key (proveedor_id) references proveedores (id),
    constraint fk_proveedor_bebidas_beb foreign key (bebida_id) references bebidas (id)
);

-- Compra_Proveedor del modelo lógico.
create table compras_bebidas (
    id              number(19,0) generated by default as identity,
    local_id        number(19,0)       not null,
    proveedor_id    number(19,0)       not null,
    fecha           date               not null,
    total           number(14,2)       not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_compras_bebidas primary key (id),
    constraint fk_compras_local foreign key (local_id) references locales (id),
    constraint fk_compras_proveedor foreign key (proveedor_id) references proveedores (id),
    constraint ck_compras_total check (total > 0)
);

-- Detalle_Bebida del modelo lógico: cajas o unidades compradas.
create table detalles_compra (
    id                        number(19,0) generated by default as identity,
    compra_id                 number(19,0)       not null,
    bebida_id                 number(19,0)       not null,
    presentaciones            number(10,0)       not null,
    unidades_por_presentacion number(10,0)       not null,
    costo_total               number(14,2)       not null,
    version                   number(19,0)       default 0 not null,
    creado_en                 timestamp(9) with time zone not null,
    actualizado_en            timestamp(9) with time zone not null,
    creado_por                varchar2(160 char) not null,
    actualizado_por           varchar2(160 char) not null,
    constraint pk_detalles_compra primary key (id),
    constraint fk_detalles_compra foreign key (compra_id) references compras_bebidas (id),
    constraint fk_detalles_compra_bebida foreign key (bebida_id) references bebidas (id),
    constraint ck_detalles_compra check (
        presentaciones > 0 and unidades_por_presentacion > 0 and costo_total > 0)
);

-- ---------------------------------------------------------------------
-- 8. Ventas directas
-- ---------------------------------------------------------------------
-- Orden_Venta del modelo lógico. local_id debe ser el de la caja (lo valida
-- el backend); se guarda para el correlativo por local y los reportes.
-- total = suma de (cantidad * precio_unitario) + recargo_llevar.
create table ventas (
    id               number(19,0) generated by default as identity,
    numero           varchar2(12 char)  not null,
    local_id         number(19,0)       not null,
    caja_id          number(19,0)       not null,
    cliente_id       number(19,0),
    servicio         varchar2(32 char)  not null,
    para_llevar      number(1,0)        default 0 not null,
    estado           varchar2(16 char)  not null,
    fecha            timestamp(9)       not null,
    recargo_llevar   number(14,2)       default 0 not null,
    total            number(14,2)       not null,
    anulada_en       timestamp(9),
    motivo_anulacion varchar2(250 char),
    version          number(19,0)       default 0 not null,
    creado_en        timestamp(9) with time zone not null,
    actualizado_en   timestamp(9) with time zone not null,
    creado_por       varchar2(160 char) not null,
    actualizado_por  varchar2(160 char) not null,
    constraint pk_ventas primary key (id),
    constraint uq_ventas_numero unique (local_id, numero),
    constraint fk_ventas_local foreign key (local_id) references locales (id),
    constraint fk_ventas_caja foreign key (caja_id) references sesiones_caja (id),
    constraint fk_ventas_cliente foreign key (cliente_id) references clientes (id),
    constraint fk_ventas_servicio foreign key (servicio) references servicios (codigo),
    constraint ck_ventas_para_llevar check (para_llevar in (0,1)),
    constraint ck_ventas_estado check (estado in ('PENDIENTE','PAGADA','ANULADA')),
    constraint ck_ventas_montos check (
        total > 0 and recargo_llevar >= 0 and (para_llevar = 1 or recargo_llevar = 0)),
    constraint ck_ventas_anulacion check (
        (estado = 'ANULADA' and anulada_en is not null and motivo_anulacion is not null) or
        (estado <> 'ANULADA' and anulada_en is null and motivo_anulacion is null))
);

-- Detalle_Orden: cada línea es un plato suelto, un menú (combo) o una bebida.
-- Precio y recargo se copian al registrar (quedan congelados).
-- Una línea COMBO con cantidad > 1 lleva los mismos platos; si eligen
-- platos distintos, van en líneas separadas.
create table detalles_orden (
    id               number(19,0) generated by default as identity,
    venta_id         number(19,0)       not null,
    tipo             varchar2(16 char)  not null,
    plato_id         number(19,0),
    combo_id         number(19,0),
    bebida_id        number(19,0),
    descripcion      varchar2(160 char) not null,
    cantidad         number(10,0)       not null,
    precio_unitario  number(14,2)       not null,
    recargo_unitario number(14,2)       default 0 not null,
    version          number(19,0)       default 0 not null,
    creado_en        timestamp(9) with time zone not null,
    actualizado_en   timestamp(9) with time zone not null,
    creado_por       varchar2(160 char) not null,
    actualizado_por  varchar2(160 char) not null,
    constraint pk_detalles_orden primary key (id),
    constraint fk_detalles_orden_venta foreign key (venta_id) references ventas (id),
    constraint fk_detalles_orden_plato foreign key (plato_id) references platos (id),
    constraint fk_detalles_orden_combo foreign key (combo_id) references reglas_cobro (id),
    constraint fk_detalles_orden_bebida foreign key (bebida_id) references bebidas (id),
    constraint ck_detalles_orden_tipo check (tipo in ('PLATO','COMBO','BEBIDA')),
    constraint ck_detalles_orden_linea check (
        cantidad > 0 and precio_unitario > 0 and recargo_unitario >= 0 and (
            (tipo = 'PLATO'  and plato_id  is not null and combo_id is null and bebida_id is null) or
            (tipo = 'COMBO'  and combo_id  is not null and plato_id is null and bebida_id is null) or
            (tipo = 'BEBIDA' and bebida_id is not null and plato_id is null and combo_id  is null
                and recargo_unitario = 0)))
);

-- Platos elegidos dentro de un menú (ej. desayuno: 1 quinua + 2 pan francés).
create table detalle_combo_platos (
    detalle_id      number(19,0)       not null,
    plato_id        number(19,0)       not null,
    cantidad        number(10,0)       default 1 not null,
    constraint pk_detalle_combo_platos primary key (detalle_id, plato_id),
    constraint fk_combo_platos_detalle foreign key (detalle_id) references detalles_orden (id),
    constraint fk_combo_platos_plato foreign key (plato_id) references platos (id),
    constraint ck_combo_platos_cantidad check (cantidad > 0)
);

-- Pagos de la venta: uno o varios métodos; la suma debe dar el total (backend).
create table pagos_venta (
    id              number(19,0) generated by default as identity,
    venta_id        number(19,0)       not null,
    monto           number(14,2)       not null,
    metodo          varchar2(32 char)  not null,
    fecha           timestamp(9)       not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_pagos_venta primary key (id),
    constraint fk_pagos_venta foreign key (venta_id) references ventas (id),
    constraint fk_pagos_venta_metodo foreign key (metodo) references metodos_pago (codigo),
    constraint ck_pagos_venta_monto check (monto > 0)
);

-- RF19: devolución al anular una venta pagada (por cada método cobrado).
create table devoluciones_venta (
    id              number(19,0) generated by default as identity,
    venta_id        number(19,0)       not null,
    monto           number(14,2)       not null,
    metodo          varchar2(32 char)  not null,
    fecha           timestamp(9)       not null,
    motivo          varchar2(250 char) not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_devoluciones_venta primary key (id),
    constraint fk_devoluciones_venta foreign key (venta_id) references ventas (id),
    constraint fk_devoluciones_metodo foreign key (metodo) references metodos_pago (codigo),
    constraint ck_devoluciones_monto check (monto > 0)
);

-- Kardex de bebidas por local.
create table movimientos_stock (
    id                number(19,0) generated by default as identity,
    local_id          number(19,0)       not null,
    bebida_id         number(19,0)       not null,
    tipo              varchar2(16 char)  not null,
    cantidad          number(10,0)       not null,
    fecha             timestamp(9)       not null,
    detalle_compra_id number(19,0),
    detalle_orden_id  number(19,0),
    motivo            varchar2(250 char) not null,
    version           number(19,0)       default 0 not null,
    creado_en         timestamp(9) with time zone not null,
    actualizado_en    timestamp(9) with time zone not null,
    creado_por        varchar2(160 char) not null,
    actualizado_por   varchar2(160 char) not null,
    constraint pk_movimientos_stock primary key (id),
    constraint fk_movimientos_local foreign key (local_id) references locales (id),
    constraint fk_movimientos_bebida foreign key (bebida_id) references bebidas (id),
    constraint fk_movimientos_compra foreign key (detalle_compra_id) references detalles_compra (id),
    constraint fk_movimientos_venta foreign key (detalle_orden_id) references detalles_orden (id),
    constraint ck_movimientos_tipo check (tipo in ('COMPRA','VENTA','DEVOLUCION')),
    constraint ck_movimientos_cantidad check (cantidad > 0),
    constraint ck_movimientos_origen check (
        (tipo = 'COMPRA' and detalle_compra_id is not null and detalle_orden_id is null) or
        (tipo in ('VENTA','DEVOLUCION') and detalle_orden_id is not null and detalle_compra_id is null))
);

-- ---------------------------------------------------------------------
-- 9. Gastos
-- ---------------------------------------------------------------------
-- GastoMercado del modelo lógico.
create table gastos_diarios (
    id              number(19,0) generated by default as identity,
    local_id        number(19,0)       not null,
    fecha           date               not null,
    monto           number(14,2)       not null,
    descripcion     varchar2(500 char) not null,
    activo          number(1,0)        default 1 not null,
    version         number(19,0)       default 0 not null,
    creado_en       timestamp(9) with time zone not null,
    actualizado_en  timestamp(9) with time zone not null,
    creado_por      varchar2(160 char) not null,
    actualizado_por varchar2(160 char) not null,
    constraint pk_gastos_diarios primary key (id),
    constraint fk_gastos_local foreign key (local_id) references locales (id),
    constraint ck_gastos_monto check (monto > 0),
    constraint ck_gastos_activo check (activo in (0,1))
);

-- ---------------------------------------------------------------------
-- 10. Índices sobre FK y columnas de búsqueda
--     (Oracle no indexa las FK automáticamente; las PK y UNIQUE sí.)
-- ---------------------------------------------------------------------
create index ix_empleados_local          on empleados (local_id);
create index ix_usuarios_rol             on usuarios (rol);
create index ix_tokens_usuario           on tokens_restablecimiento (usuario_id);
create index ix_plan_servicios_servicio  on plan_servicios (servicio);
create index ix_platos_categoria         on platos (categoria);
create index ix_menu_platos_plato        on menu_platos (plato_id);
create index ix_reglas_cobro_servicio    on reglas_cobro (servicio);
-- RF29: un solo menú público activo por servicio (las reglas inactivas no cuentan).
create unique index uq_reglas_cobro_activa on reglas_cobro (
    case when activo = 1 then servicio end);
create index ix_componentes_categoria    on componentes_regla (categoria);
create index ix_precios_sueltos_cat      on precios_sueltos (categoria);
create index ix_cajas_local              on sesiones_caja (local_id);
create index ix_cajas_usuario            on sesiones_caja (usuario_id);
create index ix_cierre_caja_metodo       on detalles_cierre_caja (metodo);
create index ix_contratos_cliente_fechas on contratos (cliente_id, fecha_inicio, fecha_fin);
create index ix_contratos_local_estado   on contratos (local_id, estado);
create index ix_contratos_plan           on contratos (plan_id);
-- Una sola renovación vigente por contrato; las anuladas no cuentan.
-- Las filas con anterior_id null o estado ANULADO no entran en el índice.
create unique index uq_contratos_anterior on contratos (
    case when estado <> 'ANULADO' then anterior_id end);
-- Busca la renovación de un contrato (FK fk_contratos_anterior) aunque esté anulada.
create index ix_contratos_anterior       on contratos (anterior_id);
create index ix_consumos_detalle         on consumos (detalle_contrato_id);
create index ix_consumos_fecha           on consumos (fecha);
create index ix_consumos_caja            on consumos (caja_id);
create index ix_detalles_consumo_plato   on detalles_consumo (plato_id);
create index ix_pagos_contrato_contrato  on pagos_contrato (contrato_id);
create index ix_pagos_contrato_caja      on pagos_contrato (caja_id);
create index ix_stock_bebida             on stock_bebidas (bebida_id);
create index ix_proveedor_bebidas_prov   on proveedor_bebidas (proveedor_id);
create index ix_compras_local_fecha      on compras_bebidas (local_id, fecha);
create index ix_compras_proveedor        on compras_bebidas (proveedor_id);
create index ix_detalles_compra          on detalles_compra (compra_id);
create index ix_detalles_compra_bebida   on detalles_compra (bebida_id);
create index ix_ventas_caja              on ventas (caja_id);
create index ix_ventas_cliente           on ventas (cliente_id);
create index ix_ventas_local_fecha       on ventas (local_id, fecha);
create index ix_detalles_orden_venta     on detalles_orden (venta_id);
create index ix_detalles_orden_plato     on detalles_orden (plato_id);
create index ix_detalles_orden_combo     on detalles_orden (combo_id);
create index ix_detalles_orden_bebida    on detalles_orden (bebida_id);
create index ix_combo_platos_plato       on detalle_combo_platos (plato_id);
create index ix_pagos_venta_venta        on pagos_venta (venta_id);
create index ix_devoluciones_venta       on devoluciones_venta (venta_id);
create index ix_movimientos_local_beb    on movimientos_stock (local_id, bebida_id);
create index ix_movimientos_compra       on movimientos_stock (detalle_compra_id);
create index ix_movimientos_orden        on movimientos_stock (detalle_orden_id);
create index ix_gastos_local_fecha       on gastos_diarios (local_id, fecha);

-- ---------------------------------------------------------------------
-- 11. Datos iniciales
--     El backend (CatalogosBootstrap) agrega solo los códigos que falten,
--     así que se puede ejecutar este bloque o dejar que lo haga la app.
-- ---------------------------------------------------------------------
insert into roles (codigo, nombre, descripcion) values ('ADMINISTRADOR', 'Administrador', 'Operación del negocio, gestiona solo cajeros');
insert into roles (codigo, nombre, descripcion) values ('DUENO', 'Dueño', 'Acceso total, incluidos usuarios y locales');
insert into roles (codigo, nombre, descripcion) values ('CAJERO', 'Cajero', 'Consumos, ventas y caja');

insert into metodos_pago (codigo, nombre) values ('EFECTIVO', 'Efectivo');
insert into metodos_pago (codigo, nombre) values ('YAPE', 'Yape');
insert into metodos_pago (codigo, nombre) values ('PLIN', 'Plin');
insert into metodos_pago (codigo, nombre) values ('TARJETA', 'Tarjeta');
insert into metodos_pago (codigo, nombre) values ('TRANSFERENCIA', 'Transferencia');

insert into categorias_plato (codigo, nombre) values ('PAN', 'Pan');
insert into categorias_plato (codigo, nombre, descripcion) values ('BEBIDA_DESAYUNO', 'Bebida de desayuno', 'Soya, quinua, maca, avena...');
insert into categorias_plato (codigo, nombre) values ('ENTRADA', 'Entrada');
insert into categorias_plato (codigo, nombre) values ('SEGUNDO', 'Segundo');
insert into categorias_plato (codigo, nombre) values ('REFRESCO', 'Refresco');

insert into servicios (codigo, nombre) values ('D', 'Desayuno');
insert into servicios (codigo, nombre) values ('A', 'Almuerzo');
insert into servicios (codigo, nombre) values ('C', 'Cena');

-- D y A: lunes a viernes. C: lunes a jueves.
insert into servicio_dias_atencion (servicio, dia_semana) select 'D', level from dual connect by level <= 5;
insert into servicio_dias_atencion (servicio, dia_semana) select 'A', level from dual connect by level <= 5;
insert into servicio_dias_atencion (servicio, dia_semana) select 'C', level from dual connect by level <= 4;

insert into locales (nombre, creado_en, actualizado_en, creado_por, actualizado_por)
values ('Local principal', systimestamp, systimestamp, 'script', 'script');

insert into correlativos (local_id, tipo)
select id, 'VENTA' from locales where nombre = 'Local principal' union all
select id, 'CONTRATO' from locales where nombre = 'Local principal';

-- Planes: ofrecen D, A y C; el cliente elige cuáles contrata.
insert into tipos_plan (nombre, periodo, creado_en, actualizado_en, creado_por, actualizado_por)
values ('Semanal', 'SEMANAL', systimestamp, systimestamp, 'script', 'script');
insert into tipos_plan (nombre, periodo, creado_en, actualizado_en, creado_por, actualizado_por)
values ('Mensual', 'MENSUAL', systimestamp, systimestamp, 'script', 'script');

insert into plan_servicios (plan_id, servicio)
select p.id, s.codigo from tipos_plan p cross join servicios s;

-- Precio pensionista por ración.
insert into tarifas_servicio (servicio, precio, creado_en, actualizado_en, creado_por, actualizado_por)
values ('D', 3.20, systimestamp, systimestamp, 'script', 'script');
insert into tarifas_servicio (servicio, precio, creado_en, actualizado_en, creado_por, actualizado_por)
values ('A', 10.00, systimestamp, systimestamp, 'script', 'script');
insert into tarifas_servicio (servicio, precio, creado_en, actualizado_en, creado_por, actualizado_por)
values ('C', 8.00, systimestamp, systimestamp, 'script', 'script');

-- Menú completo al público y recargo para llevar.
insert into reglas_cobro (nombre, servicio, precio_combo, recargo_llevar, creado_en, actualizado_en, creado_por, actualizado_por)
values ('Desayuno', 'D', 4.50, 0.00, systimestamp, systimestamp, 'script', 'script');
insert into reglas_cobro (nombre, servicio, precio_combo, recargo_llevar, creado_en, actualizado_en, creado_por, actualizado_por)
values ('Menú almuerzo', 'A', 11.00, 2.00, systimestamp, systimestamp, 'script', 'script');
insert into reglas_cobro (nombre, servicio, precio_combo, recargo_llevar, creado_en, actualizado_en, creado_por, actualizado_por)
values ('Cena', 'C', 9.00, 2.00, systimestamp, systimestamp, 'script', 'script');

-- Composición: D = 1 líquido + 2 panes; A = entrada + segundo + refresco; C = segundo + refresco.
insert into componentes_regla (regla_id, categoria, cantidad)
select r.id, c.categoria, c.cantidad
from reglas_cobro r
join (select 'D' servicio, 'BEBIDA_DESAYUNO' categoria, 1 cantidad from dual union all
      select 'D', 'PAN',      2 from dual union all
      select 'A', 'ENTRADA',  1 from dual union all
      select 'A', 'SEGUNDO',  1 from dual union all
      select 'A', 'REFRESCO', 1 from dual union all
      select 'C', 'SEGUNDO',  1 from dual union all
      select 'C', 'REFRESCO', 1 from dual) c on c.servicio = r.servicio;

-- Sueltos al público. La cena solo vende el menú completo.
insert into precios_sueltos (servicio, categoria, precio, recargo_llevar) values ('D', 'PAN',             1.50, 0.00);
insert into precios_sueltos (servicio, categoria, precio, recargo_llevar) values ('D', 'BEBIDA_DESAYUNO', 1.50, 0.00);
insert into precios_sueltos (servicio, categoria, precio, recargo_llevar) values ('A', 'ENTRADA',         4.00, 1.00);
insert into precios_sueltos (servicio, categoria, precio, recargo_llevar) values ('A', 'SEGUNDO',        10.00, 1.00);

commit;

-- ---------------------------------------------------------------------
-- 12. Documentación en el diccionario de datos
-- ---------------------------------------------------------------------
comment on table roles is 'Catálogo de roles de usuario, la PK es el código usado por el backend';
comment on table metodos_pago is 'Catálogo de métodos de pago (Tipo_Pago del modelo lógico)';
comment on table categorias_plato is 'Catálogo de categorías de plato (Categoria del modelo lógico)';
comment on table servicios is 'Catálogo de servicios D/A/C (Turno y Servicio del modelo lógico)';
comment on table servicio_dias_atencion is 'Días de la semana (ISO) en que se atiende cada servicio';
comment on table locales is 'Sucursales del restaurante, base del crecimiento multi-local';
comment on table dias_no_atencion is 'Feriados o cierres por local, no cuentan como raciones';
comment on table correlativos is 'Último número de venta y contrato por local';
comment on table personas is 'Datos personales comunes a clientes y empleados';
comment on table clientes is 'Especialización de persona: pensionista o cliente de venta';
comment on table empleados is 'Especialización de persona: trabajador asignado a un local';
comment on table usuarios is 'Cuenta de acceso de un empleado';
comment on table tipos_plan is 'Plan de pensión: periodo semanal o mensual';
comment on table plan_servicios is 'Servicios que ofrece cada plan';
comment on table tarifas_servicio is 'Precio por ración para pensionistas (CU_14)';
comment on table reglas_cobro is 'Menú completo al público: precio, composición y recargo para llevar (CU_13)';
comment on table componentes_regla is 'Composición del menú por categoría, valida ventas y consumos';
comment on table precios_sueltos is 'Precio suelto al público por servicio y categoría';
comment on table programaciones_menu is 'Menú programado por local, fecha y servicio';
comment on table sesiones_caja is 'Apertura y cierre de caja por usuario y local';
comment on table detalles_cierre_caja is 'Arqueo de cierre por método de pago';
comment on table contratos is 'Contrato de pensión prepago, renovar crea otro enlazado por anterior_id';
comment on table detalles_contrato is 'Servicios contratados con raciones, saldo y precio congelado';
comment on table consumos is 'Ración consumida por un pensionista (Control_Cliente del modelo lógico)';
comment on table pagos_contrato is 'Pago adelantado del contrato y devolución si se anula';
comment on table ventas is 'Venta directa (Orden_Venta del modelo lógico)';
comment on table detalles_orden is 'Líneas de venta: plato suelto, menú o bebida';
comment on table detalle_combo_platos is 'Platos elegidos dentro de una línea de menú';
comment on table stock_bebidas is 'Inventario de bebidas por local';
comment on table movimientos_stock is 'Kardex de bebidas por local';
comment on table compras_bebidas is 'Compra a proveedor (Compra_Proveedor del modelo lógico)';
comment on table detalles_compra is 'Detalle de compra por cajas o unidades (Detalle_Bebida del modelo lógico)';
comment on table gastos_diarios is 'Gastos globales diarios por local (GastoMercado del modelo lógico)';

comment on column contratos.fecha_fin is 'Semanal: inicio + 6. Mensual: inicio + 1 mes - 1. Al pasar, el contrato finaliza aunque tenga saldo';
comment on column contratos.anterior_id is 'Contrato del que proviene esta renovación; único entre contratos no anulados';
comment on column detalles_contrato.raciones_contratadas is 'Días de atención del servicio entre inicio y fin, sin días de no atención';
comment on column detalles_contrato.saldo_raciones is 'Raciones aún disponibles, las no usadas se pierden al finalizar';
comment on column consumos.clave_diaria is 'cliente:fecha:servicio, una ración por servicio por día. Se pone null al anular';
comment on column consumos.recargo_llevar is 'Recargo cobrado al pensionista por llevar, entra en la caja indicada';
comment on column sesiones_caja.clave_abierta is 'Id del usuario mientras la caja está abierta, una caja abierta por usuario';
comment on column ventas.recargo_llevar is 'Suma de cantidad * recargo_unitario de las líneas';
comment on column detalles_orden.recargo_unitario is 'Recargo para llevar por unidad, 0 en bebidas y desayuno';

-- ---------------------------------------------------------------------
-- 13. Solo lectura para reportes (ROL_MANA_CONSULTA)
--     El rol y el usuario MANA_CONSULTA los crea mana-usuarios-roles.sql
--     (como SYSTEM). Si el rol no existe, este bloque avisa y continúa;
--     después puede ejecutar solo esta sección.
--     * Privilegio READ (Oracle 12.1.0.2+): permite consultar pero no
--       SELECT ... FOR UPDATE, así que la cuenta de consulta no puede
--       bloquear filas que usa el backend.
--     * Lista explícita: una tabla nueva NO queda expuesta hasta agregarla aquí.
--     * Datos sensibles solo por vistas con columnas explícitas:
--         v_personas  sin DNI ni teléfono
--         v_usuarios  sin correo, clave_hash ni token_version
--       No se conceden personas, usuarios, tokens_restablecimiento ni
--       correlativos (control interno del backend).
-- ---------------------------------------------------------------------
create or replace view v_personas as
select id, nombre, apellido
  from personas;

create or replace view v_usuarios as
select id, empleado_id, rol, activo
  from usuarios;

comment on table v_personas is 'Personas sin DNI ni teléfono, para consultas de solo lectura';
comment on table v_usuarios is 'Usuarios sin correo ni clave_hash, para consultas de solo lectura';

-- Mensajes del bloque: en DBeaver active "Mostrar salida del servidor" (pestaña Output); en SQL*Plus use: set serveroutput on
declare
    e_rol_inexistente exception;
    pragma exception_init(e_rol_inexistente, -1917);  -- ORA-01917: usuario o rol inexistente
    type t_lista is table of varchar2(128);
    v_objetos t_lista := t_lista(
        -- Vistas con columnas explícitas
        'V_PERSONAS', 'V_USUARIOS',
        -- Catálogos y configuración
        'ROLES', 'METODOS_PAGO', 'CATEGORIAS_PLATO', 'SERVICIOS', 'SERVICIO_DIAS_ATENCION',
        'LOCALES', 'DIAS_NO_ATENCION', 'TIPOS_PLAN', 'PLAN_SERVICIOS', 'TARIFAS_SERVICIO',
        'PLATOS', 'PROGRAMACIONES_MENU', 'MENU_PLATOS', 'REGLAS_COBRO', 'COMPONENTES_REGLA',
        'PRECIOS_SUELTOS',
        -- Especializaciones sin datos personales (id, local, cargo, activo)
        'CLIENTES', 'EMPLEADOS',
        -- Operación: caja, pensiones, ventas, inventario y gastos
        'SESIONES_CAJA', 'DETALLES_CIERRE_CAJA',
        'CONTRATOS', 'DETALLES_CONTRATO', 'CONSUMOS', 'DETALLES_CONSUMO', 'PAGOS_CONTRATO',
        'VENTAS', 'DETALLES_ORDEN', 'DETALLE_COMBO_PLATOS', 'PAGOS_VENTA', 'DEVOLUCIONES_VENTA',
        'BEBIDAS', 'STOCK_BEBIDAS', 'PROVEEDORES', 'PROVEEDOR_BEBIDAS', 'COMPRAS_BEBIDAS',
        'DETALLES_COMPRA', 'MOVIMIENTOS_STOCK', 'GASTOS_DIARIOS');
begin
    for i in 1 .. v_objetos.count loop
        execute immediate 'grant read on ' || v_objetos(i) || ' to rol_mana_consulta';
    end loop;
    dbms_output.put_line('ROL_MANA_CONSULTA: READ otorgado en ' || v_objetos.count || ' tablas y vistas');
exception
    when e_rol_inexistente then
        dbms_output.put_line('ROL_MANA_CONSULTA no existe: ejecute mana-usuarios-roles.sql como SYSTEM y luego solo esta sección');
end;
/

-- ---------------------------------------------------------------------
-- 14. Verificación (debe dar 0 objetos inválidos y los catálogos cargados)
-- ---------------------------------------------------------------------
select (select count(*) from user_tables)                                as tablas,
       (select count(*) from user_objects where status = 'INVALID')      as invalidos,
       (select count(*) from roles)                                      as roles,
       (select count(*) from metodos_pago)                               as metodos_pago,
       (select count(*) from servicio_dias_atencion)                     as dias_atencion,
       (select count(*) from tarifas_servicio)                           as tarifas,
       (select count(*) from reglas_cobro)                               as menus_publicos,
       (select count(*) from correlativos)                               as correlativos
from dual;
