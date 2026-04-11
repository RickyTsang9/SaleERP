use sale_erp;

alter table wms_inbound
add column purchase_order_id bigint(20) default null comment 'source purchase order id' after inbound_type;

create index idx_wms_inbound_purchase_order_id on wms_inbound (purchase_order_id);
