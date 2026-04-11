use sale_erp;

alter table wms_inbound_item
add column purchase_order_item_id bigint(20) default null comment 'source purchase order item id' after inbound_id;

create index idx_wms_inbound_item_purchase_order_item_id on wms_inbound_item (purchase_order_item_id);
