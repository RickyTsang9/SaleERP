-- saleerp 高频查询索引补充脚本

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_purchase_order'
              and index_name = 'idx_wms_purchase_order_supplier_status'
        ),
        'select 1',
        'create index idx_wms_purchase_order_supplier_status on wms_purchase_order (supplier_id, status)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_sale_order'
              and index_name = 'idx_wms_sale_order_order_no'
        ),
        'select 1',
        'create index idx_wms_sale_order_order_no on wms_sale_order (order_no)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_stock'
              and index_name = 'idx_wms_stock_warehouse_product_location_batch'
        ),
        'select 1',
        'create index idx_wms_stock_warehouse_product_location_batch on wms_stock (warehouse_id, product_id, location_id, batch_no)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'md_customer'
              and index_name = 'idx_md_customer_customer_code'
        ),
        'select 1',
        'create index idx_md_customer_customer_code on md_customer (customer_code)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'md_product'
              and index_name = 'idx_md_product_product_code'
        ),
        'select 1',
        'create index idx_md_product_product_code on md_product (product_code)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'crm_customer_follow'
              and index_name = 'idx_crm_customer_follow_customer_id'
        ),
        'select 1',
        'create index idx_crm_customer_follow_customer_id on crm_customer_follow (customer_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_sale_return'
              and index_name = 'idx_wms_sale_return_customer_id'
        ),
        'select 1',
        'create index idx_wms_sale_return_customer_id on wms_sale_return (customer_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_outbound'
              and index_name = 'idx_wms_outbound_customer_id'
        ),
        'select 1',
        'create index idx_wms_outbound_customer_id on wms_outbound (customer_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_purchase_return'
              and index_name = 'idx_wms_purchase_return_supplier_id'
        ),
        'select 1',
        'create index idx_wms_purchase_return_supplier_id on wms_purchase_return (supplier_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_inbound'
              and index_name = 'idx_wms_inbound_supplier_id'
        ),
        'select 1',
        'create index idx_wms_inbound_supplier_id on wms_inbound (supplier_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'md_location'
              and index_name = 'idx_md_location_warehouse_id'
        ),
        'select 1',
        'create index idx_md_location_warehouse_id on md_location (warehouse_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_stock'
              and index_name = 'idx_wms_stock_location_id'
        ),
        'select 1',
        'create index idx_wms_stock_location_id on wms_stock (location_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_stock'
              and index_name = 'idx_wms_stock_product_id'
        ),
        'select 1',
        'create index idx_wms_stock_product_id on wms_stock (product_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_stock'
              and index_name = 'idx_wms_stock_warehouse_id'
        ),
        'select 1',
        'create index idx_wms_stock_warehouse_id on wms_stock (warehouse_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_stock_log'
              and index_name = 'idx_wms_stock_log_location_id'
        ),
        'select 1',
        'create index idx_wms_stock_log_location_id on wms_stock_log (location_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_stock_log'
              and index_name = 'idx_wms_stock_log_product_id'
        ),
        'select 1',
        'create index idx_wms_stock_log_product_id on wms_stock_log (product_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_stock_log'
              and index_name = 'idx_wms_stock_log_warehouse_id'
        ),
        'select 1',
        'create index idx_wms_stock_log_warehouse_id on wms_stock_log (warehouse_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_sale_return'
              and index_name = 'idx_wms_sale_return_warehouse_id'
        ),
        'select 1',
        'create index idx_wms_sale_return_warehouse_id on wms_sale_return (warehouse_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_purchase_return'
              and index_name = 'idx_wms_purchase_return_warehouse_id'
        ),
        'select 1',
        'create index idx_wms_purchase_return_warehouse_id on wms_purchase_return (warehouse_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_inventory_check'
              and index_name = 'idx_wms_inventory_check_warehouse_id'
        ),
        'select 1',
        'create index idx_wms_inventory_check_warehouse_id on wms_inventory_check (warehouse_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_transfer'
              and index_name = 'idx_wms_transfer_out_warehouse_id'
        ),
        'select 1',
        'create index idx_wms_transfer_out_warehouse_id on wms_transfer (out_warehouse_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_transfer'
              and index_name = 'idx_wms_transfer_in_warehouse_id'
        ),
        'select 1',
        'create index idx_wms_transfer_in_warehouse_id on wms_transfer (in_warehouse_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'biz_executive_brief_record'
              and index_name = 'idx_biz_executive_brief_record_brief_date_brief_id'
        ),
        'select 1',
        'create index idx_biz_executive_brief_record_brief_date_brief_id on biz_executive_brief_record (brief_date, brief_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'biz_operation_snapshot'
              and index_name = 'idx_biz_operation_snapshot_snapshot_date_snapshot_id'
        ),
        'select 1',
        'create index idx_biz_operation_snapshot_snapshot_date_snapshot_id on biz_operation_snapshot (snapshot_date, snapshot_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_payable'
              and index_name = 'idx_fin_payable_supplier_status_payable_id'
        ),
        'select 1',
        'create index idx_fin_payable_supplier_status_payable_id on fin_payable (supplier_id, status, payable_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_purchase_order'
              and index_name = 'idx_wms_purchase_order_status_create_time'
        ),
        'select 1',
        'create index idx_wms_purchase_order_status_create_time on wms_purchase_order (status, create_time)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_inbound'
              and index_name = 'idx_wms_inbound_status_create_time'
        ),
        'select 1',
        'create index idx_wms_inbound_status_create_time on wms_inbound (status, create_time)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_outbound'
              and index_name = 'idx_wms_outbound_status_create_time'
        ),
        'select 1',
        'create index idx_wms_outbound_status_create_time on wms_outbound (status, create_time)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_receivable'
              and index_name = 'idx_fin_receivable_customer_status_receivable_id'
        ),
        'select 1',
        'create index idx_fin_receivable_customer_status_receivable_id on fin_receivable (customer_id, status, receivable_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'biz_message'
              and index_name = 'idx_biz_message_status_type_id'
        ),
        'select 1',
        'create index idx_biz_message_status_type_id on biz_message (status, message_type, message_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'sys_notice'
              and index_name = 'idx_sys_notice_status_notice_id'
        ),
        'select 1',
        'create index idx_sys_notice_status_notice_id on sys_notice (status, notice_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_purchase_order'
              and index_name = 'idx_wms_purchase_order_status'
        ),
        'select 1',
        'create index idx_wms_purchase_order_status on wms_purchase_order (status)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_purchase_order_item'
              and index_name = 'idx_wms_purchase_order_item_order_product'
        ),
        'select 1',
        'create index idx_wms_purchase_order_item_order_product on wms_purchase_order_item (purchase_order_id, product_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_inbound'
              and index_name = 'idx_wms_inbound_status_purchase_order_id'
        ),
        'select 1',
        'create index idx_wms_inbound_status_purchase_order_id on wms_inbound (status, purchase_order_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_sale_order'
              and index_name = 'idx_wms_sale_order_customer_status'
        ),
        'select 1',
        'create index idx_wms_sale_order_customer_status on wms_sale_order (customer_id, status)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_sale_order'
              and index_name = 'idx_wms_sale_order_warehouse_status'
        ),
        'select 1',
        'create index idx_wms_sale_order_warehouse_status on wms_sale_order (warehouse_id, status)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_sale_order'
              and index_name = 'idx_wms_sale_order_status_create_time'
        ),
        'select 1',
        'create index idx_wms_sale_order_status_create_time on wms_sale_order (status, create_time)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_outbound'
              and index_name = 'idx_wms_outbound_status_sale_order_id'
        ),
        'select 1',
        'create index idx_wms_outbound_status_sale_order_id on wms_outbound (status, sale_order_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'wms_outbound'
              and index_name = 'idx_wms_outbound_status_source_order_no'
        ),
        'select 1',
        'create index idx_wms_outbound_status_source_order_no on wms_outbound (status, source_order_no)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_receivable'
              and index_name = 'idx_fin_receivable_status_due_date'
        ),
        'select 1',
        'create index idx_fin_receivable_status_due_date on fin_receivable (status, due_date)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_receivable'
              and index_name = 'idx_fin_receivable_customer_status'
        ),
        'select 1',
        'create index idx_fin_receivable_customer_status on fin_receivable (customer_id, status)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_receipt'
              and index_name = 'idx_fin_receipt_sale_order_id'
        ),
        'select 1',
        'create index idx_fin_receipt_sale_order_id on fin_receipt (sale_order_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_receipt'
              and index_name = 'idx_fin_receipt_customer_receipt_date'
        ),
        'select 1',
        'create index idx_fin_receipt_customer_receipt_date on fin_receipt (customer_id, receipt_date)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_payable'
              and index_name = 'idx_fin_payable_purchase_order_id'
        ),
        'select 1',
        'create index idx_fin_payable_purchase_order_id on fin_payable (purchase_order_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_payable'
              and index_name = 'idx_fin_payable_supplier_status_due_date'
        ),
        'select 1',
        'create index idx_fin_payable_supplier_status_due_date on fin_payable (supplier_id, status, due_date)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_payment'
              and index_name = 'idx_fin_payment_payable_id'
        ),
        'select 1',
        'create index idx_fin_payment_payable_id on fin_payment (payable_id)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

set @index_sql = (
    select if(
        exists(
            select 1
            from information_schema.statistics
            where table_schema = database()
              and table_name = 'fin_payment'
              and index_name = 'idx_fin_payment_supplier_payment_time'
        ),
        'select 1',
        'create index idx_fin_payment_supplier_payment_time on fin_payment (supplier_id, payment_time)'
    )
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;
