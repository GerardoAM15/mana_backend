-- =====================================================================
-- MANÁ: usuarios y roles de la base de datos
-- ---------------------------------------------------------------------
-- Ejecutar conectado como SYSTEM a la PDB (XE local: servicio xepdb1),
-- ANTES de mana-hibrido-v4.sql. En DBeaver: conexión SYSTEM, Alt + X.
--
--   MANADB             dueño del esquema; lo usa el backend Spring Boot.
--   MANA_CONSULTA      solo lectura, para reportes o revisar datos sin
--                      riesgo de modificar nada.
--   ROL_MANA_CONSULTA  rol de solo lectura. MANADB le concede READ en la
--                      sección 13 de mana-hibrido-v4.sql (este script solo
--                      crea el rol; sin esa sección el rol no ve nada).
--
-- Sirve tanto para una instalación nueva como para una existente:
--   * Si MANADB no existe, lo crea con la clave indicada abajo.
--   * Si ya existe, NO cambia su clave; solo revisa sus permisos.
--   * Lo mismo con ROL_MANA_CONSULTA y MANA_CONSULTA.
-- Se puede ejecutar más de una vez sin error.
--
-- ANTES DE EJECUTAR: cambie las dos claves del bloque. El script se
-- detiene si siguen con el valor CAMBIE_ESTA_CLAVE. Use la misma clave de
-- MANADB en DB_PASSWORD o en application-local.yaml del backend.
-- Las claves no deben contener comillas dobles.
-- =====================================================================

-- Mensajes del bloque: en DBeaver active "Mostrar salida del servidor" (pestaña Output); en SQL*Plus use: set serveroutput on
declare
    -- >>> Edite estas dos claves (solo se usan si el usuario todavía no existe) <<<
    v_clave_manadb    varchar2(128) := 'CAMBIE_ESTA_CLAVE';
    v_clave_consulta  varchar2(128) := 'CAMBIE_ESTA_CLAVE';

    c_marcador constant varchar2(30) := 'CAMBIE_ESTA_CLAVE';

    function existe_usuario(p_usuario varchar2) return boolean is
        v_n number;
    begin
        select count(*) into v_n from dba_users where username = p_usuario;
        return v_n > 0;
    end;

    function existe_rol(p_rol varchar2) return boolean is
        v_n number;
    begin
        select count(*) into v_n from dba_roles where role = p_rol;
        return v_n > 0;
    end;

    procedure validar_clave(p_usuario varchar2, p_clave varchar2) is
    begin
        if p_clave is null or p_clave = c_marcador then
            raise_application_error(-20001,
                'Defina la clave de ' || p_usuario || ' en este script antes de ejecutarlo');
        end if;
        if instr(p_clave, '"') > 0 then
            raise_application_error(-20002,
                'La clave de ' || p_usuario || ' no puede contener comillas dobles');
        end if;
    end;
begin
    -- 1. Esquema de la aplicación
    if existe_usuario('MANADB') then
        dbms_output.put_line('MANADB ya existe: se conserva su clave');
    else
        validar_clave('MANADB', v_clave_manadb);
        execute immediate 'create user manadb identified by "' || v_clave_manadb || '"'
                       || ' default tablespace users quota unlimited on users';
        dbms_output.put_line('MANADB creado');
    end if;
    execute immediate 'grant create session, create table, create view, create sequence,'
                   || ' create procedure, create trigger to manadb';
    execute immediate 'alter user manadb quota unlimited on users';

    -- 2. Rol de solo lectura (sus permisos los concede MANADB en la sección 13 del v4)
    if existe_rol('ROL_MANA_CONSULTA') then
        dbms_output.put_line('ROL_MANA_CONSULTA ya existe');
    else
        execute immediate 'create role rol_mana_consulta';
        dbms_output.put_line('ROL_MANA_CONSULTA creado');
    end if;

    -- 3. Usuario de consulta
    if existe_usuario('MANA_CONSULTA') then
        dbms_output.put_line('MANA_CONSULTA ya existe: se conserva su clave');
    else
        validar_clave('MANA_CONSULTA', v_clave_consulta);
        execute immediate 'create user mana_consulta identified by "' || v_clave_consulta || '"'
                       || ' default tablespace users';
        dbms_output.put_line('MANA_CONSULTA creado');
    end if;
    execute immediate 'grant create session to mana_consulta';
    execute immediate 'grant rol_mana_consulta to mana_consulta';
end;
/

-- 4. Verificación: deben aparecer MANADB y MANA_CONSULTA (OPEN) y el rol.
select username, account_status from dba_users
 where username in ('MANADB', 'MANA_CONSULTA')
 order by username;
select role from dba_roles where role = 'ROL_MANA_CONSULTA';

-- 5. Prueba (ejecútela después de mana-hibrido-v4.sql), conectado como MANA_CONSULTA:
--   select count(*) from manadb.contratos;                -- funciona
--   select nombre, apellido from manadb.v_personas;       -- funciona
--   select rol, activo from manadb.v_usuarios;            -- funciona
--   select dni from manadb.personas;                      -- ORA-00942: sin acceso
--   select clave_hash from manadb.usuarios;               -- ORA-00942: sin acceso
--   select * from manadb.contratos for update;            -- ORA-01031: READ no permite bloquear
--   delete from manadb.contratos;                         -- ORA-01031: privilegios insuficientes
