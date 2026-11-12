alter table contratos drop constraint uq_contratos_anterior;
create unique index uq_contratos_anterior on contratos (case when estado <> 'ANULADO' then anterior_id end);
