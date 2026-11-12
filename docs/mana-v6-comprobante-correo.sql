-- =====================================================================
-- MANÁ v6: envío de comprobantes electrónicos por correo
-- Ejecutar como el dueño del esquema (MANADB / PROYECTO_MANA) DESPUÉS de mana-v5-comprobantes.sql.
-- =====================================================================

alter table comprobantes add (
    receptor_email     varchar2(120 char),
    correo_enviado_en  timestamp(9)
);

comment on column comprobantes.receptor_email is 'Correo del cliente para enviarle el PDF y el XML (opcional)';
comment on column comprobantes.correo_enviado_en is 'Último envío exitoso del comprobante por correo; null = no enviado';

-- Verificación: debe devolver 2 filas.
select column_name, data_type, data_length
  from user_tab_columns
 where table_name = 'COMPROBANTES'
   and column_name in ('RECEPTOR_EMAIL', 'CORREO_ENVIADO_EN');
