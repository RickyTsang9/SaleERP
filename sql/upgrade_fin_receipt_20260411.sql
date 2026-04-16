set @database_name = (select database());

set @payment_time_column_count = (
    select count(1)
    from information_schema.columns
    where table_schema = @database_name
      and table_name = 'fin_receipt'
      and column_name = 'payment_time'
);

set @add_payment_time_sql = if(
    @payment_time_column_count = 0,
    'alter table fin_receipt add column payment_time datetime null comment ''回款时间'' after amount',
    'select 1'
);
prepare add_payment_time_statement from @add_payment_time_sql;
execute add_payment_time_statement;
deallocate prepare add_payment_time_statement;

set @payment_method_column_count = (
    select count(1)
    from information_schema.columns
    where table_schema = @database_name
      and table_name = 'fin_receipt'
      and column_name = 'payment_method'
);

set @add_payment_method_sql = if(
    @payment_method_column_count = 0,
    'alter table fin_receipt add column payment_method varchar(50) default null comment ''回款方式'' after payment_time',
    'select 1'
);
prepare add_payment_method_statement from @add_payment_method_sql;
execute add_payment_method_statement;
deallocate prepare add_payment_method_statement;

set @receipt_date_column_count = (
    select count(1)
    from information_schema.columns
    where table_schema = @database_name
      and table_name = 'fin_receipt'
      and column_name = 'receipt_date'
);

set @sync_payment_time_sql = if(
    @receipt_date_column_count > 0,
    'update fin_receipt set payment_time = ifnull(payment_time, receipt_date) where receipt_date is not null',
    'select 1'
);
prepare sync_payment_time_statement from @sync_payment_time_sql;
execute sync_payment_time_statement;
deallocate prepare sync_payment_time_statement;
