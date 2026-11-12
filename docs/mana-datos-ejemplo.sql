-- =====================================================================
-- MANÁ: datos de ejemplo para desarrollo y demostración
-- ---------------------------------------------------------------------
-- Ejecutar UNA sola vez, DESPUÉS de mana-hibrido-v4.sql, conectado como
-- MANADB (en DBeaver: Alt + X). Solo para desarrollo: las contraseñas son
-- de prueba; no use este script en producción.
-- Para repetirlo: mana-drop.sql, mana-hibrido-v4.sql y luego este script.
--
-- Crea:
--   * 3 usuarios: dueno@mana.pe / Dueno12345 (DUENO)
--                 admin@mana.pe / Admin12345 (ADMINISTRADOR)
--                 cajero@mana.pe / Cajero12345 (CAJERO)
--   * 3 clientes, 16 platos, el menú de los próximos 14 días de atención,
--     3 bebidas, 1 proveedor y una compra inicial con su stock.
-- Como ya existe un DUENO, el backend no crea otro usuario inicial.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Usuarios (persona + empleado + usuario). Claves en BCrypt.
-- ---------------------------------------------------------------------
insert into personas (dni, nombre, apellido, creado_en, actualizado_en, creado_por, actualizado_por)
values ('00000002', 'Dueño', 'Prueba', systimestamp, systimestamp, 'script', 'script');
insert into personas (dni, nombre, apellido, creado_en, actualizado_en, creado_por, actualizado_por)
values ('00000001', 'Administrador', 'MANÁ', systimestamp, systimestamp, 'script', 'script');
insert into personas (dni, nombre, apellido, creado_en, actualizado_en, creado_por, actualizado_por)
values ('00000003', 'Cajero', 'Prueba', systimestamp, systimestamp, 'script', 'script');

insert into empleados (id, local_id, cargo)
select p.id, l.id, c.cargo
from personas p
join (select '00000002' dni, 'Dueño' cargo from dual union all
      select '00000001', 'Administrador' from dual union all
      select '00000003', 'Cajero' from dual) c on c.dni = p.dni
cross join (select id from locales where nombre = 'Local principal') l;

insert into usuarios (empleado_id, correo, clave_hash, rol, creado_en, actualizado_en, creado_por, actualizado_por)
select p.id, u.correo, u.clave, u.rol, systimestamp, systimestamp, 'script', 'script'
from personas p
join (select '00000002' dni, 'dueno@mana.pe' correo, '$2a$10$bIyAyRpJCjt3MpLKPdpzUOSuyG2OGdmTOiDUSl23DA.P7s73SiXYu' clave, 'DUENO' rol from dual union all
      select '00000001', 'admin@mana.pe', '$2a$10$PdjdiOqXHNOx9UvFM97pVeSCfIlrVjrAuX84OqaLac2BtaFMRoG6m', 'ADMINISTRADOR' from dual union all
      select '00000003', 'cajero@mana.pe', '$2a$10$49F0AAMBaRSmNyec0WZLkOMhLyHEC9T6e3f90XNMcXQ0tYDfidxZK', 'CAJERO' from dual) u
  on u.dni = p.dni;

-- ---------------------------------------------------------------------
-- 2. Clientes de prueba
-- ---------------------------------------------------------------------
insert into personas (dni, nombre, apellido, telefono, creado_en, actualizado_en, creado_por, actualizado_por)
select dni, nombre, apellido, telefono, systimestamp, systimestamp, 'script', 'script' from (
  select '70000001' dni, 'Lucía' nombre, 'Quispe Mamani' apellido, '987654321' telefono from dual union all
  select '70000002', 'Carlos', 'Huamán Torres', '987654322' from dual union all
  select '70000003', 'María', 'Condori Flores', '987654323' from dual);

insert into clientes (id)
select id from personas where dni in ('70000001', '70000002', '70000003');

-- ---------------------------------------------------------------------
-- 3. Banco de platos
-- ---------------------------------------------------------------------
insert into platos (nombre, categoria, creado_en, actualizado_en, creado_por, actualizado_por)
select nombre, categoria, systimestamp, systimestamp, 'script', 'script' from (
  select 'Pan francés' nombre, 'PAN' categoria from dual union all
  select 'Pan de yema', 'PAN' from dual union all
  select 'Pan integral', 'PAN' from dual union all
  select 'Quinua', 'BEBIDA_DESAYUNO' from dual union all
  select 'Soya', 'BEBIDA_DESAYUNO' from dual union all
  select 'Maca', 'BEBIDA_DESAYUNO' from dual union all
  select 'Avena', 'BEBIDA_DESAYUNO' from dual union all
  select 'Sopa de verduras', 'ENTRADA' from dual union all
  select 'Papa a la huancaína', 'ENTRADA' from dual union all
  select 'Arroz con pollo', 'SEGUNDO' from dual union all
  select 'Ají de gallina', 'SEGUNDO' from dual union all
  select 'Guiso de pollo', 'SEGUNDO' from dual union all
  select 'Pollo a la plancha', 'SEGUNDO' from dual union all
  select 'Milanesa de pollo', 'SEGUNDO' from dual union all
  select 'Chicha morada', 'REFRESCO' from dual union all
  select 'Maracuyá', 'REFRESCO' from dual);

-- ---------------------------------------------------------------------
-- 4. Menú de los próximos 14 días, solo en los días de atención de cada
--    servicio (D y A: lunes a viernes; C: lunes a jueves).
--    Desayuno: panes y líquidos. Almuerzo: entradas, 3 segundos y
--    refrescos. Cena: guiso del almuerzo, pollo a la plancha, milanesa y
--    refrescos.
-- ---------------------------------------------------------------------
declare
  v_local  number;
  v_menu   number;
  v_fecha  date;
  v_dia    number;
begin
  select id into v_local from locales where nombre = 'Local principal';
  for i in 0 .. 13 loop
    v_fecha := trunc(sysdate) + i;
    v_dia   := trunc(v_fecha) - trunc(v_fecha, 'IW') + 1;   -- 1 = lunes ... 7 = domingo
    for s in (select servicio from servicio_dias_atencion where dia_semana = v_dia order by servicio) loop
      insert into programaciones_menu (local_id, fecha, servicio, creado_en, actualizado_en, creado_por, actualizado_por)
      values (v_local, v_fecha, s.servicio, systimestamp, systimestamp, 'script', 'script')
      returning id into v_menu;
      insert into menu_platos (menu_id, plato_id)
      select v_menu, p.id from platos p
      where (s.servicio = 'D' and p.categoria in ('PAN', 'BEBIDA_DESAYUNO'))
         or (s.servicio = 'A' and (p.categoria in ('ENTRADA', 'REFRESCO')
                                   or p.nombre in ('Arroz con pollo', 'Ají de gallina', 'Guiso de pollo')))
         or (s.servicio = 'C' and (p.categoria = 'REFRESCO'
                                   or p.nombre in ('Guiso de pollo', 'Pollo a la plancha', 'Milanesa de pollo')));
    end loop;
  end loop;
end;
/

-- ---------------------------------------------------------------------
-- 5. Bebidas, proveedor y compra inicial (con stock y kardex)
-- ---------------------------------------------------------------------
insert into bebidas (nombre, precio_venta, creado_en, actualizado_en, creado_por, actualizado_por)
select nombre, precio, systimestamp, systimestamp, 'script', 'script' from (
  select 'Inca Kola 500 ml' nombre, 3.00 precio from dual union all
  select 'Coca Cola 500 ml', 3.00 from dual union all
  select 'Agua San Luis 625 ml', 2.00 from dual);

insert into proveedores (razon_social, telefono, creado_en, actualizado_en, creado_por, actualizado_por)
values ('Distribuidora Lima S.A.C.', '014567890', systimestamp, systimestamp, 'script', 'script');

insert into proveedor_bebidas (proveedor_id, bebida_id)
select pr.id, b.id from proveedores pr cross join bebidas b
where pr.razon_social = 'Distribuidora Lima S.A.C.';

declare
  v_local  number;
  v_prov   number;
  v_compra number;
  v_det    number;
  v_unid   number;
begin
  select id into v_local from locales where nombre = 'Local principal';
  select id into v_prov from proveedores where razon_social = 'Distribuidora Lima S.A.C.';
  insert into compras_bebidas (local_id, proveedor_id, fecha, total, creado_en, actualizado_en, creado_por, actualizado_por)
  values (v_local, v_prov, trunc(sysdate), 108.00, systimestamp, systimestamp, 'script', 'script')
  returning id into v_compra;
  -- 2 paquetes x 12 de cada bebida
  for b in (select id, nombre from bebidas order by id) loop
    v_unid := 24;
    insert into detalles_compra (compra_id, bebida_id, presentaciones, unidades_por_presentacion, costo_total,
                                 creado_en, actualizado_en, creado_por, actualizado_por)
    values (v_compra, b.id, 2, 12, 36.00, systimestamp, systimestamp, 'script', 'script')
    returning id into v_det;
    insert into stock_bebidas (local_id, bebida_id, stock, creado_en, actualizado_en, creado_por, actualizado_por)
    values (v_local, b.id, v_unid, systimestamp, systimestamp, 'script', 'script');
    insert into movimientos_stock (local_id, bebida_id, tipo, cantidad, fecha, detalle_compra_id, motivo,
                                   creado_en, actualizado_en, creado_por, actualizado_por)
    values (v_local, b.id, 'COMPRA', v_unid, systimestamp, v_det, 'Compra inicial de ejemplo',
            systimestamp, systimestamp, 'script', 'script');
  end loop;
end;
/

commit;

-- ---------------------------------------------------------------------
-- Verificación (debe dar 3 usuarios, 3 clientes, 16 platos, 3 bebidas)
-- ---------------------------------------------------------------------
select (select count(*) from usuarios)            as usuarios,
       (select count(*) from clientes)            as clientes,
       (select count(*) from platos)              as platos,
       (select count(*) from programaciones_menu) as menus,
       (select count(*) from bebidas)             as bebidas,
       (select sum(stock) from stock_bebidas)     as stock_total
from dual;
