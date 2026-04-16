USE sale_erp;

-- 先清理可能存在的冲突数据
delete from wms_outbound where outbound_no = 'OUT20231026001';
delete from wms_inbound where inbound_no in ('IN20231026001', 'IN20231026002');
delete from fin_receipt where sale_order_id in (
  select sale_order_id
  from wms_sale_order
  where order_no in ('SO20231020001', 'SO20231021001', 'SO20231022001')
);
delete from fin_receivable where sale_order_id in (
  select sale_order_id
  from wms_sale_order
  where order_no in ('SO20231020001', 'SO20231021001', 'SO20231022001')
);
delete from wms_sale_order_item where sale_order_id in (select sale_order_id from wms_sale_order where order_no in ('SO20231020001', 'SO20231021001', 'SO20231022001', 'SO20231022002', 'SO20231023001', 'SO20231024001', 'SO20231025001', 'SO20231026001', 'SO20231026002'));
delete from wms_sale_order where order_no in ('SO20231020001', 'SO20231021001', 'SO20231022001', 'SO20231022002', 'SO20231023001', 'SO20231024001', 'SO20231025001', 'SO20231026001', 'SO20231026002');
delete from wms_purchase_order where order_no in ('PO20231024001', 'PO20231024002');
delete from wms_stock where batch_no in ('B20231001', 'B20231002', 'B20231003');
delete from md_customer where customer_code in ('C1001', 'C1002', 'C1003', 'C1004', 'C1005', 'C1006');
delete from md_product where product_code in ('P10001', 'P10002', 'P20001', 'P20002', 'P30001');

-- 1. 插入商品基础数据 (手机、电脑、配件)
INSERT INTO md_product (product_code, product_name, product_spec, unit_name, bar_code, category_name, brand_name, cost_price, sale_price, status, create_by, create_time) VALUES 
('P10001', 'iPhone 15 Pro', '256GB', '台', '6901234567890', '手机通讯', 'Apple', 6800.00, 7999.00, '0', 'admin', sysdate()),
('P10002', '华为 Mate 60 Pro', '512GB', '台', '6901234567891', '手机通讯', '华为', 6200.00, 6999.00, '0', 'admin', sysdate()),
('P20001', 'MacBook Air M2', '8G 256G', '台', '6901234567892', '电脑办公', 'Apple', 7500.00, 8999.00, '0', 'admin', sysdate()),
('P20002', 'ThinkPad X1 Carbon', '16G 512G', '台', '6901234567893', '电脑办公', '联想', 8500.00, 9999.00, '0', 'admin', sysdate()),
('P30001', 'AirPods Pro', '二代', '副', '6901234567894', '数码配件', 'Apple', 1400.00, 1899.00, '0', 'admin', sysdate());

-- 2. 插入客户基础数据
INSERT INTO md_customer (customer_code, customer_name, contact_person, contact_phone, address, credit_limit, payment_method, customer_level, status, create_by, create_time) VALUES 
('C1001', '北京华数科技有限公司', '张经理', '13800138000', '北京市海淀区中关村软件园1号楼', 100000.00, 'bank_transfer', 'vip', '0', 'admin', sysdate()),
('C1002', '上海天创商贸有限公司', '李总', '13900139000', '上海市浦东新区张江高科技园区', 50000.00, 'credit', 'gold', '0', 'admin', sysdate()),
('C1003', '广州智联网络科技', '王总监', '13700137000', '广州市天河区天河软件园', 20000.00, 'cash', 'silver', '0', 'admin', sysdate()),
('C1004', '深圳微客电子通讯', '赵老板', '13600136000', '深圳市南山区科技园南区', 0.00, 'cash', 'normal', '0', 'admin', sysdate()),
('C1005', '杭州云创数据服务', '孙主管', '13500135000', '杭州市滨江区网商路', 80000.00, 'bank_transfer', 'vip', '0', 'admin', sysdate()),
('C1006', '成都飞跃电子科技', '周总', '13400134000', '成都市高新区天府软件园', 30000.00, 'credit', 'gold', '0', 'admin', sysdate());

-- 3. 插入库存数据及预警数据 (部分正常，部分触发预警)
INSERT INTO wms_stock (warehouse_id, product_id, location_id, batch_no, quantity, locked_quantity, frozen_quantity, warning_min_qty, warning_max_qty, create_by, create_time) VALUES 
(1, 1, 1, 'B20231001', 50, 0, 0, 20, 200, 'admin', sysdate()), -- 正常
(1, 2, 1, 'B20231001', 15, 0, 0, 30, 200, 'admin', sysdate()), -- 触发最小预警 (库存15 < 预警30)
(1, 3, 2, 'B20231002', 8, 0, 0, 10, 100, 'admin', sysdate()),  -- 触发最小预警 (库存8 < 预警10)
(1, 4, 2, 'B20231002', 120, 0, 0, 10, 100, 'admin', sysdate()), -- 触发最大预警 (库存120 > 预警100)
(1, 5, 3, 'B20231003', 300, 0, 0, 50, 500, 'admin', sysdate()); -- 正常

-- 4. 插入采购单数据 (待入库)
insert into wms_purchase_order (order_no, supplier_id, purchase_date, total_amount, status, remark, create_by, create_time) values
('PO20231024001', 1, date_sub(curdate(), interval 1 day), 124000.00, '2', '演示采购单-部分入库', 'admin', date_sub(now(), interval 1 day)),
('PO20231024002', 2, curdate(), 75000.00, '1', '演示采购单-待入库', 'admin', now());

-- 5. 插入销售单主表数据 (模拟近期销售趋势，按 create_time 分配到最近几天，状态为 audited 以计入销售额)
insert into wms_sale_order (order_no, customer_id, warehouse_id, total_qty, total_amount, status, create_by, create_time) values
('SO20231020001', 1, 1, 10, 79990.00, 'audited', 'admin', date_sub(now(), interval 6 day)),
('SO20231021001', 2, 1, 5, 34995.00, 'audited', 'admin', date_sub(now(), interval 5 day)),
('SO20231022001', 3, 1, 8, 71992.00, 'audited', 'admin', date_sub(now(), interval 4 day)),
('SO20231022002', 4, 1, 2, 19998.00, 'audited', 'admin', date_sub(now(), interval 4 day)),
('SO20231023001', 5, 1, 15, 28485.00, 'audited', 'admin', date_sub(now(), interval 3 day)),
('SO20231024001', 1, 1, 6, 47994.00, 'audited', 'admin', date_sub(now(), interval 2 day)),
('SO20231025001', 6, 1, 4, 35996.00, 'audited', 'admin', date_sub(now(), interval 1 day)),
('SO20231026001', 2, 1, 3, 20997.00, 'audited', 'admin', now()),
('SO20231026002', 1, 1, 5, 39995.00, 'submitted', 'admin', now()); -- 待出库(submitted)

-- 6. 插入销售单明细数据
-- SO20231020001
INSERT INTO wms_sale_order_item (sale_order_id, product_id, location_id, batch_no, quantity, price, amount) 
SELECT sale_order_id, 1, 1, 'B20231001', 10, 7999.00, 79990.00 FROM wms_sale_order WHERE order_no = 'SO20231020001';

-- SO20231021001
INSERT INTO wms_sale_order_item (sale_order_id, product_id, location_id, batch_no, quantity, price, amount) 
SELECT sale_order_id, 2, 1, 'B20231001', 5, 6999.00, 34995.00 FROM wms_sale_order WHERE order_no = 'SO20231021001';

-- SO20231022001
INSERT INTO wms_sale_order_item (sale_order_id, product_id, location_id, batch_no, quantity, price, amount) 
SELECT sale_order_id, 3, 2, 'B20231002', 8, 8999.00, 71992.00 FROM wms_sale_order WHERE order_no = 'SO20231022001';

-- SO20231022002
INSERT INTO wms_sale_order_item (sale_order_id, product_id, location_id, batch_no, quantity, price, amount) 
SELECT sale_order_id, 4, 2, 'B20231002', 2, 9999.00, 19998.00 FROM wms_sale_order WHERE order_no = 'SO20231022002';

-- SO20231023001
INSERT INTO wms_sale_order_item (sale_order_id, product_id, location_id, batch_no, quantity, price, amount) 
SELECT sale_order_id, 5, 3, 'B20231003', 15, 1899.00, 28485.00 FROM wms_sale_order WHERE order_no = 'SO20231023001';

-- SO20231024001
INSERT INTO wms_sale_order_item (sale_order_id, product_id, location_id, batch_no, quantity, price, amount) 
SELECT sale_order_id, 1, 1, 'B20231001', 6, 7999.00, 47994.00 FROM wms_sale_order WHERE order_no = 'SO20231024001';

-- SO20231025001
INSERT INTO wms_sale_order_item (sale_order_id, product_id, location_id, batch_no, quantity, price, amount) 
SELECT sale_order_id, 3, 2, 'B20231002', 4, 8999.00, 35996.00 FROM wms_sale_order WHERE order_no = 'SO20231025001';

-- SO20231026001
INSERT INTO wms_sale_order_item (sale_order_id, product_id, location_id, batch_no, quantity, price, amount) 
SELECT sale_order_id, 2, 1, 'B20231001', 3, 6999.00, 20997.00 FROM wms_sale_order WHERE order_no = 'SO20231026001';

-- SO20231026002 (待出库)
INSERT INTO wms_sale_order_item (sale_order_id, product_id, location_id, batch_no, quantity, price, amount) 
SELECT sale_order_id, 1, 1, 'B20231001', 5, 7999.00, 39995.00 FROM wms_sale_order WHERE order_no = 'SO20231026002';

-- 6.1 修正演示销售数据关联到当前主数据主键
update wms_sale_order
set customer_id = (select customer_id from md_customer where customer_code = 'C1001' limit 1)
where order_no in ('SO20231020001', 'SO20231024001', 'SO20231026002');

update wms_sale_order
set customer_id = (select customer_id from md_customer where customer_code = 'C1002' limit 1)
where order_no in ('SO20231021001', 'SO20231026001');

update wms_sale_order
set customer_id = (select customer_id from md_customer where customer_code = 'C1003' limit 1)
where order_no = 'SO20231022001';

update wms_sale_order
set customer_id = (select customer_id from md_customer where customer_code = 'C1004' limit 1)
where order_no = 'SO20231022002';

update wms_sale_order
set customer_id = (select customer_id from md_customer where customer_code = 'C1005' limit 1)
where order_no = 'SO20231023001';

update wms_sale_order
set customer_id = (select customer_id from md_customer where customer_code = 'C1006' limit 1)
where order_no = 'SO20231025001';

update wms_sale_order_item item_record
join wms_sale_order sale_order_record on item_record.sale_order_id = sale_order_record.sale_order_id
set item_record.product_id = (select product_id from md_product where product_code = 'P10001' limit 1)
where sale_order_record.order_no in ('SO20231020001', 'SO20231024001', 'SO20231026002');

update wms_sale_order_item item_record
join wms_sale_order sale_order_record on item_record.sale_order_id = sale_order_record.sale_order_id
set item_record.product_id = (select product_id from md_product where product_code = 'P10002' limit 1)
where sale_order_record.order_no in ('SO20231021001', 'SO20231026001');

update wms_sale_order_item item_record
join wms_sale_order sale_order_record on item_record.sale_order_id = sale_order_record.sale_order_id
set item_record.product_id = (select product_id from md_product where product_code = 'P20001' limit 1)
where sale_order_record.order_no in ('SO20231022001', 'SO20231025001');

update wms_sale_order_item item_record
join wms_sale_order sale_order_record on item_record.sale_order_id = sale_order_record.sale_order_id
set item_record.product_id = (select product_id from md_product where product_code = 'P20002' limit 1)
where sale_order_record.order_no = 'SO20231022002';

update wms_sale_order_item item_record
join wms_sale_order sale_order_record on item_record.sale_order_id = sale_order_record.sale_order_id
set item_record.product_id = (select product_id from md_product where product_code = 'P30001' limit 1)
where sale_order_record.order_no = 'SO20231023001';

-- 7. 插入应收账款及收款记录 (模拟财务数据)
-- 应收记录1: 已结清
insert into fin_receivable (sale_order_id, customer_id, amount_due, amount_paid, due_date, status, create_by, create_time)
select sale_order_id, 1, 79990.00, 79990.00, date_add(curdate(), interval 7 day), 'paid', 'admin', date_sub(now(), interval 6 day)
from wms_sale_order
where order_no = 'SO20231020001';

-- 收款记录1
insert into fin_receipt (receivable_id, sale_order_id, customer_id, amount, receipt_date, create_by, create_time)
select receivable_id, sale_order_id, customer_id, 79990.00, date_sub(curdate(), interval 5 day), 'admin', date_sub(now(), interval 5 day)
from fin_receivable
where sale_order_id = (select sale_order_id from wms_sale_order where order_no = 'SO20231020001');

-- 应收记录2: 部分收款 (赊销)
insert into fin_receivable (sale_order_id, customer_id, amount_due, amount_paid, due_date, status, create_by, create_time)
select sale_order_id, 2, 34995.00, 10000.00, date_add(curdate(), interval 10 day), 'partial', 'admin', date_sub(now(), interval 5 day)
from wms_sale_order
where order_no = 'SO20231021001';

-- 收款记录2
insert into fin_receipt (receivable_id, sale_order_id, customer_id, amount, receipt_date, create_by, create_time)
select receivable_id, sale_order_id, customer_id, 10000.00, date_sub(curdate(), interval 3 day), 'admin', date_sub(now(), interval 3 day)
from fin_receivable
where sale_order_id = (select sale_order_id from wms_sale_order where order_no = 'SO20231021001');

-- 应收记录3: 逾期未收 (赊销，到期日为昨天)
insert into fin_receivable (sale_order_id, customer_id, amount_due, amount_paid, due_date, status, create_by, create_time)
select sale_order_id, 3, 71992.00, 0.00, date_sub(curdate(), interval 1 day), 'unpaid', 'admin', date_sub(now(), interval 30 day)
from wms_sale_order
where order_no = 'SO20231022001';

-- 8. 插入入库/出库单 (模拟待出入库指标)
-- 待入库
insert into wms_inbound (inbound_no, inbound_type, purchase_order_id, supplier_id, warehouse_id, total_qty, total_amount, status, create_by, create_time)
select 'IN20231026001', 'purchase', purchase_order_id, supplier_id, 1, 100.00, 124000.00, 'submitted', 'admin', now()
from wms_purchase_order
where order_no = 'PO20231024001'
union all
select 'IN20231026002', 'purchase', purchase_order_id, supplier_id, 1, 50.00, 75000.00, 'draft', 'admin', now()
from wms_purchase_order
where order_no = 'PO20231024002';

-- 待出库
insert into wms_outbound (outbound_no, outbound_type, customer_id, sale_order_id, sale_order_no, warehouse_id, total_qty, total_amount, status, source_order_no, create_by, create_time)
select 'OUT20231026001', 'sale', customer_id, sale_order_id, order_no, warehouse_id, 5.00, 39995.00, 'submitted', order_no, 'admin', now()
from wms_sale_order
where order_no = 'SO20231026002';
