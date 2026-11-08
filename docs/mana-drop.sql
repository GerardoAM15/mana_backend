-- =====================================================================
-- MANÁ: vaciar el esquema (borra TODAS las tablas y TODOS los datos)
-- ---------------------------------------------------------------------
-- Ejecutar conectado como MANADB (en DBeaver: Alt + X) solo cuando quiera
-- reinstalar desde cero con mana-hibrido-v4.sql. No se puede deshacer:
-- las tablas se borran con PURGE (no pasan a la papelera).
--
-- Solo borra los objetos de MANÁ listados abajo; si alguno no existe, lo
-- omite. No toca los usuarios MANADB ni MANA_CONSULTA ni el rol
-- ROL_MANA_CONSULTA (esos los administra SYSTEM).
-- =====================================================================

-- Mensajes del bloque: en DBeaver active "Mostrar salida del servidor" (pestaña Output); en SQL*Plus use: set serveroutput on
declare
    e_no_existe exception;
    pragma exception_init(e_no_existe, -942);   -- ORA-00942: tabla o vista inexistente
    type t_lista is table of varchar2(128);
    -- Orden inverso al de creación: primero las tablas que dependen de otras.
    v_tablas t_lista := t_lista(
        'GASTOS_DIARIOS', 'MOVIMIENTOS_STOCK', 'DEVOLUCIONES_VENTA', 'PAGOS_VENTA',
        'DETALLE_COMBO_PLATOS', 'DETALLES_ORDEN', 'VENTAS', 'DETALLES_COMPRA',
        'COMPRAS_BEBIDAS', 'PROVEEDOR_BEBIDAS', 'PROVEEDORES', 'STOCK_BEBIDAS',
        'BEBIDAS', 'PAGOS_CONTRATO', 'DETALLES_CONSUMO', 'CONSUMOS',
        'DETALLES_CONTRATO', 'CONTRATOS', 'DETALLES_CIERRE_CAJA', 'SESIONES_CAJA',
        'PRECIOS_SUELTOS', 'COMPONENTES_REGLA', 'REGLAS_COBRO', 'MENU_PLATOS',
        'PROGRAMACIONES_MENU', 'PLATOS', 'TARIFAS_SERVICIO', 'PLAN_SERVICIOS',
        'TIPOS_PLAN', 'TOKENS_RESTABLECIMIENTO', 'USUARIOS', 'EMPLEADOS',
        'CLIENTES', 'PERSONAS', 'CORRELATIVOS', 'DIAS_NO_ATENCION', 'LOCALES',
        'SERVICIO_DIAS_ATENCION', 'SERVICIOS', 'CATEGORIAS_PLATO', 'METODOS_PAGO',
        'ROLES');
    v_borradas number := 0;
begin
    for v in (select 'V_USUARIOS' nombre from dual union all select 'V_PERSONAS' from dual) loop
        begin
            execute immediate 'drop view ' || v.nombre;
        exception
            when e_no_existe then null;
        end;
    end loop;
    for i in 1 .. v_tablas.count loop
        begin
            execute immediate 'drop table ' || v_tablas(i) || ' cascade constraints purge';
            v_borradas := v_borradas + 1;
        exception
            when e_no_existe then null;
        end;
    end loop;
    dbms_output.put_line('Tablas de MANÁ borradas: ' || v_borradas);
end;
/

-- Verificación: debe dar 0 (o solo tablas ajenas a MANÁ).
select count(*) as tablas_restantes from user_tables;
