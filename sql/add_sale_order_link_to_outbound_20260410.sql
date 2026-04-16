alter table wms_outbound
    add column sale_order_id bigint(20) null comment '来源销售单编号' after customer_id,
    add column sale_order_no varchar(32) null comment '来源销售单号' after sale_order_id;

alter table wms_outbound
    add index idx_wms_outbound_sale_order_id (sale_order_id);

alter table wms_outbound_item
    add column sale_order_item_id bigint(20) null comment '来源销售明细编号' after outbound_id;

alter table wms_outbound_item
    add index idx_wms_outbound_item_sale_order_item_id (sale_order_item_id);
