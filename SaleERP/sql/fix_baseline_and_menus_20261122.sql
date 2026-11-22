-- SaleERP 数据库菜单补齐、字段升级与业务演示数据闭环修复脚本 (2026-11-22)

-- 1. 升级 fin_receipt 回款表字段（保留 receipt_date 兼容统计查询，补齐 payment_time 与 payment_method）
set @db_name = database();

set @col_payment_time = (
    select count(1) from information_schema.columns
    where table_schema = @db_name and table_name = 'fin_receipt' and column_name = 'payment_time'
);
set @sql_payment_time = if(
    @col_payment_time = 0,
    'alter table fin_receipt add column payment_time datetime null comment ''回款时间'' after amount',
    'select 1'
);
prepare stmt_pt from @sql_payment_time;
execute stmt_pt;
deallocate prepare stmt_pt;

set @col_payment_method = (
    select count(1) from information_schema.columns
    where table_schema = @db_name and table_name = 'fin_receipt' and column_name = 'payment_method'
);
set @sql_payment_method = if(
    @col_payment_method = 0,
    'alter table fin_receipt add column payment_method varchar(50) default null comment ''回款方式'' after payment_time',
    'select 1'
);
prepare stmt_pm from @sql_payment_method;
execute stmt_pm;
deallocate prepare stmt_pm;

update fin_receipt
set payment_time = ifnull(payment_time, receipt_date),
    payment_method = ifnull(nullif(payment_method, ''), 'bank_transfer')
where receipt_id > 0;

-- 2. 规范化 sys_menu 类型大小写，补齐供应商管理、客户跟进侧边栏入口，优化统一待办中心层级与图标
update sys_menu
set menu_type = upper(menu_type)
where menu_type in ('m', 'c', 'f');

-- 供应商管理挂载到“基础资料(3023)”下
update sys_menu
set parent_id = 3023,
    order_num = 3,
    path = 'supplier',
    component = 'business/supplier/index',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'business:supplier:list',
    icon = 'peoples',
    remark = '供应商管理菜单'
where menu_id = 5205;

-- 客户跟进挂载到“基础资料(3023)”下
update sys_menu
set parent_id = 3023,
    order_num = 4,
    path = 'customerFollow',
    component = 'business/customerFollow/index',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'business:customerFollow:list',
    icon = 'people',
    remark = '客户跟进菜单'
where menu_id = 5237;

-- 统一待办中心提升为一级菜单并修复有效 SVG 图标 checkbox
update sys_menu
set parent_id = 0,
    order_num = 0,
    path = 'todoCenter',
    component = 'business/todoCenter/index',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'business:todoCenter:list',
    icon = 'checkbox',
    remark = '统一待办中心菜单'
where menu_id = 5300;

-- 补齐普通角色菜单授权
insert into sys_role_menu(role_id, menu_id)
select 2, m.menu_id
from sys_menu m
where m.menu_id in (5205, 5206, 5207, 5208, 5209, 5210, 5237, 5238, 5239, 5240, 5241, 5242, 5300, 5301)
  and not exists (
    select 1 from sys_role_menu rm where rm.role_id = 2 and rm.menu_id = m.menu_id
  );

-- 3. 补齐供应商主数据 (md_supplier)
insert into md_supplier (supplier_id, supplier_code, supplier_name, contact_person, contact_phone, contact_email, address, status, del_flag, create_by, create_time, remark)
select 1, 'SUP1001', '深圳华强数码供应链有限公司', '陈海波', '13823100001', 'chenhb@hqsupply.cn', '深圳市福田区华强北路赛格科技园2栋', '0', '0', 'admin', now(), '核心数码终端供应商'
from dual where not exists (select 1 from md_supplier where supplier_id = 1);

insert into md_supplier (supplier_id, supplier_code, supplier_name, contact_person, contact_phone, contact_email, address, status, del_flag, create_by, create_time, remark)
select 2, 'SUP1002', '上海智汇信息设备有限公司', '林立群', '13918200002', 'linlq@zhihui-it.cn', '上海市漕河泾新兴技术开发区宜山路889号', '0', '0', 'admin', now(), '电脑及办公设备供应商'
from dual where not exists (select 1 from md_supplier where supplier_id = 2);

insert into md_supplier (supplier_id, supplier_code, supplier_name, contact_person, contact_phone, contact_email, address, status, del_flag, create_by, create_time, remark)
select 3, 'SUP1003', '广东清泉饮品实业有限公司', '梁国栋', '13726300003', 'lianggd@qingquan.cn', '广州市黄埔区科学城开源大道18号', '0', '0', 'admin', now(), '食品饮料供应商'
from dual where not exists (select 1 from md_supplier where supplier_id = 3);

insert into md_supplier (supplier_id, supplier_code, supplier_name, contact_person, contact_phone, contact_email, address, status, del_flag, create_by, create_time, remark)
select 4, 'SUP1004', '东莞洁雅日用品制造厂', '黄敏华', '13635400004', 'huangmh@jieya-dg.cn', '东莞市长安镇振安东路66号', '0', '0', 'admin', now(), '纸品与家居百货供应商'
from dual where not exists (select 1 from md_supplier where supplier_id = 4);

insert into md_supplier (supplier_id, supplier_code, supplier_name, contact_person, contact_phone, contact_email, address, status, del_flag, create_by, create_time, remark)
select 5, 'SUP1005', '佛山净彩日化科技有限公司', '罗志强', '13544500005', 'luozq@jingcai-chem.cn', '佛山市顺德区大良工业园兴业路12号', '0', '0', 'admin', now(), '洗护日化供应商'
from dual where not exists (select 1 from md_supplier where supplier_id = 5);

-- 4. 补齐采购订单明细 (wms_purchase_order_item) 并修复采购入库与采购退货外键
insert into wms_purchase_order_item (purchase_order_item_id, purchase_order_id, product_id, quantity, price, amount, remark)
select 1001, 102, 990012, 20.00, 6200.00, 124000.00, '华为 Mate 60 Pro 首批采购'
from dual where exists (select 1 from wms_purchase_order where purchase_order_id = 102)
  and not exists (select 1 from wms_purchase_order_item where purchase_order_id = 102);

insert into wms_purchase_order_item (purchase_order_item_id, purchase_order_id, product_id, quantity, price, amount, remark)
select 1002, 103, 990013, 10.00, 7500.00, 75000.00, 'MacBook Air M2 季度备货'
from dual where exists (select 1 from wms_purchase_order where purchase_order_id = 103)
  and not exists (select 1 from wms_purchase_order_item where purchase_order_id = 103);

update wms_inbound set supplier_id = 3 where inbound_id in (990001, 990002, 990005) and supplier_id is null;
update wms_inbound set supplier_id = 4 where inbound_id = 990003 and supplier_id is null;
update wms_inbound set supplier_id = 5 where inbound_id = 990004 and supplier_id is null;

update wms_inbound
set purchase_order_id = 102,
    supplier_id = 1,
    warehouse_id = 990001,
    total_qty = 20.00,
    total_amount = 124000.00
where inbound_id = 990006;

update wms_inbound
set purchase_order_id = 103,
    supplier_id = 2,
    warehouse_id = 990002,
    total_qty = 10.00,
    total_amount = 75000.00
where inbound_id = 990007;

insert into wms_inbound_item (inbound_item_id, inbound_id, purchase_order_item_id, product_id, location_id, batch_no, quantity, price, amount)
select 990006, 990006, 1001, 990012, 990001, 'B20231001', 20.00, 6200.00, 124000.00
from dual where exists (select 1 from wms_inbound where inbound_id = 990006)
  and not exists (select 1 from wms_inbound_item where inbound_id = 990006);

insert into wms_inbound_item (inbound_item_id, inbound_id, purchase_order_item_id, product_id, location_id, batch_no, quantity, price, amount)
select 990007, 990007, 1002, 990013, 990002, 'B20231002', 10.00, 7500.00, 75000.00
from dual where exists (select 1 from wms_inbound where inbound_id = 990007)
  and not exists (select 1 from wms_inbound_item where inbound_id = 990007);

update wms_purchase_return set supplier_id = 3 where purchase_return_id in (990001, 990002, 990005) and supplier_id is null;
update wms_purchase_return set supplier_id = 4 where purchase_return_id = 990003 and supplier_id is null;
update wms_purchase_return set supplier_id = 5 where purchase_return_id = 990004 and supplier_id is null;

-- 5. 修复销售订单、明细、状态历史与销售出库外键
update wms_sale_order_item set location_id = 990001 where location_id = 1;
update wms_sale_order_item set location_id = 990002 where location_id = 2;
update wms_sale_order_item set location_id = 990003 where location_id = 3;

update wms_sale_order so
join (
    select sale_order_id, min(location_id) as first_loc_id
    from wms_sale_order_item
    group by sale_order_id
) item_loc on item_loc.sale_order_id = so.sale_order_id
join md_location ml on ml.location_id = item_loc.first_loc_id
set so.warehouse_id = ml.warehouse_id
where so.warehouse_id not in (select warehouse_id from md_warehouse);

update wms_sale_order set warehouse_id = 990001 where warehouse_id = 1;

update wms_sale_order
set manager_audit_by = ifnull(manager_audit_by, 'admin'),
    manager_audit_time = ifnull(manager_audit_time, date_add(create_time, interval 1 hour)),
    manager_audit_comment = ifnull(manager_audit_comment, '销售经理审核通过'),
    audit_by = ifnull(audit_by, 'admin'),
    audit_time = ifnull(audit_time, date_add(create_time, interval 2 hour)),
    finance_audit_comment = ifnull(finance_audit_comment, '财务复核通过')
where status = 'audited';

update wms_sale_order set payment_status = 'paid' where sale_order_id = 1;
update wms_sale_order set payment_status = 'partial' where sale_order_id = 2;

-- 为现有销售订单补齐状态流转历史
insert into wms_sale_order_status_history (sale_order_id, from_status, to_status, operation_type, audit_role, audit_by, audit_comment, operate_time, create_by, create_time)
select so.sale_order_id, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', so.create_time, 'admin', so.create_time
from wms_sale_order so
where so.status in ('submitted', 'manager_approved', 'audited')
  and not exists (
    select 1 from wms_sale_order_status_history h where h.sale_order_id = so.sale_order_id and h.to_status = 'submitted'
  );

insert into wms_sale_order_status_history (sale_order_id, from_status, to_status, operation_type, audit_role, audit_by, audit_comment, operate_time, create_by, create_time)
select so.sale_order_id, 'submitted', 'manager_approved', 'manager_approve', 'manager', 'admin', '销售经理审核通过', date_add(so.create_time, interval 1 hour), 'admin', date_add(so.create_time, interval 1 hour)
from wms_sale_order so
where so.status in ('manager_approved', 'audited')
  and not exists (
    select 1 from wms_sale_order_status_history h where h.sale_order_id = so.sale_order_id and h.to_status = 'manager_approved'
  );

insert into wms_sale_order_status_history (sale_order_id, from_status, to_status, operation_type, audit_role, audit_by, audit_comment, operate_time, create_by, create_time)
select so.sale_order_id, 'manager_approved', 'audited', 'finance_approve', 'finance', 'admin', '财务审核通过并确认额度', date_add(so.create_time, interval 2 hour), 'admin', date_add(so.create_time, interval 2 hour)
from wms_sale_order so
where so.status = 'audited'
  and not exists (
    select 1 from wms_sale_order_status_history h where h.sale_order_id = so.sale_order_id and h.to_status = 'audited'
  );

-- 修复销售出库单关联与明细
update wms_outbound
set customer_id = 990018,
    sale_order_id = 9,
    sale_order_no = 'SO20231026002',
    warehouse_id = 990001,
    delivery_status = ifnull(delivery_status, 'pending')
where outbound_id = 990006;

update wms_outbound set warehouse_id = 990001 where warehouse_id = 1;
update wms_outbound_item set location_id = 990001 where location_id = 1;

insert into wms_outbound_item (outbound_item_id, outbound_id, sale_order_item_id, product_id, location_id, batch_no, quantity, price, amount)
select 990006, 990006, 9, 990011, 990001, 'B20231001', 5.00, 7999.00, 39995.00
from dual where exists (select 1 from wms_outbound where outbound_id = 990006)
  and not exists (select 1 from wms_outbound_item where outbound_id = 990006);

-- 6. 修复库存台账 (wms_stock) 的仓库、库位与商品关联
update wms_stock set warehouse_id = 990001, product_id = 990011, location_id = 990001 where stock_id = 990016;
update wms_stock set warehouse_id = 990001, product_id = 990012, location_id = 990001 where stock_id = 990017;
update wms_stock set warehouse_id = 990002, product_id = 990013, location_id = 990002 where stock_id = 990018;
update wms_stock set warehouse_id = 990002, product_id = 990014, location_id = 990002 where stock_id = 990019;
update wms_stock set warehouse_id = 990003, product_id = 990015, location_id = 990003 where stock_id = 990020;

-- 7. 同步应收台账与回款记录客户主键 (fin_receivable / fin_receipt)
update fin_receivable fr
join wms_sale_order so on so.sale_order_id = fr.sale_order_id
set fr.customer_id = so.customer_id
where fr.customer_id != so.customer_id;

update fin_receipt rec
join wms_sale_order so on so.sale_order_id = rec.sale_order_id
set rec.customer_id = so.customer_id
where rec.customer_id != so.customer_id;

-- 8. 补充应付、付款、客户跟进、销售退货、盘点、调拨闭环样例数据
insert into fin_payable (payable_id, purchase_order_id, supplier_id, amount_due, amount_paid, remain_amount, status, due_date, overdue_days, remark, create_by, create_time, amount, paid_amount)
select 1001, 102, 1, 124000.00, 50000.00, 74000.00, '1', date_add(curdate(), interval 5 day), 0, 'Mate 60 Pro 采购首批结算', 'admin', now(), 124000.00, 50000.00
from dual where not exists (select 1 from fin_payable where payable_id = 1001);

insert into fin_payable (payable_id, purchase_order_id, supplier_id, amount_due, amount_paid, remain_amount, status, due_date, overdue_days, remark, create_by, create_time, amount, paid_amount)
select 1002, 103, 2, 75000.00, 0.00, 75000.00, '0', date_add(curdate(), interval 12 day), 0, 'MacBook Air M2 账期应付', 'admin', now(), 75000.00, 0.00
from dual where not exists (select 1 from fin_payable where payable_id = 1002);

insert into fin_payment (payment_id, payable_id, purchase_order_id, supplier_id, amount, payment_time, payment_method, remark, create_by, create_time, payment_date)
select 1001, 1001, 102, 1, 50000.00, now(), 'bank_transfer', '采购首付款银行转账', 'admin', now(), now()
from dual where not exists (select 1 from fin_payment where payment_id = 1001);

insert into crm_customer_follow (follow_id, customer_id, follow_type, follow_time, follow_content, next_follow_time, status, create_by, create_time)
select 1001, 990020, 'phone', date_sub(now(), interval 2 day), '沟通逾期应收账款 ￥71,992.00 回款安排，客户承诺本周五前安排财务打款。', date_add(curdate(), interval 2 day), '0', 'admin', now()
from dual where not exists (select 1 from crm_customer_follow where follow_id = 1001);

insert into crm_customer_follow (follow_id, customer_id, follow_type, follow_time, follow_content, next_follow_time, status, create_by, create_time)
select 1002, 990018, 'visit', date_sub(now(), interval 5 day), '上门拜访张经理，确认下季度终端设备集采框架与交付批次。', curdate(), '0', 'admin', now()
from dual where not exists (select 1 from crm_customer_follow where follow_id = 1002);

insert into crm_customer_follow (follow_id, customer_id, follow_type, follow_time, follow_content, next_follow_time, status, create_by, create_time)
select 1003, 990019, 'wechat', date_sub(now(), interval 7 day), '确认订单 SO20231026001 到货签收情况，客户反馈设备运行良好。', null, '1', 'admin', now()
from dual where not exists (select 1 from crm_customer_follow where follow_id = 1003);

insert into wms_sale_return (sale_return_id, return_no, return_type, customer_id, warehouse_id, total_qty, total_amount, status, audit_by, audit_time, create_by, create_time, remark)
select 1001, 'SR202605010001', 'sale_return', 990018, 990001, 1.00, 7999.00, 'draft', null, null, 'admin', now(), '客户外包装破损申请换退（草稿待审）'
from dual where not exists (select 1 from wms_sale_return where sale_return_id = 1001);

insert into wms_sale_return_item (sale_return_item_id, sale_return_id, product_id, quantity, price, amount)
select 1001, 1001, 990011, 1.00, 7999.00, 7999.00
from dual where not exists (select 1 from wms_sale_return_item where sale_return_item_id = 1001);

insert into wms_inventory_check (check_id, check_no, warehouse_id, status, total_diff_qty, total_diff_amount, audit_by, audit_time, create_by, create_time, remark)
select 1001, 'IC202605010001', 990001, 'draft', 0.00, 0.00, null, null, 'admin', now(), '华南一号仓月度例行抽盘单'
from dual where not exists (select 1 from wms_inventory_check where check_id = 1001);

insert into wms_inventory_check_item (check_item_id, check_id, product_id, location_id, batch_no, stock_qty, actual_qty, diff_qty, price, diff_amount)
select 1001, 1001, 990011, 990001, 'B20231001', 50.00, 50.00, 0.00, 6800.00, 0.00
from dual where not exists (select 1 from wms_inventory_check_item where check_item_id = 1001);

insert into wms_transfer (transfer_id, transfer_no, out_warehouse_id, in_warehouse_id, transfer_date, status, total_quantity, remark, create_by, create_time)
select 1001, 'TF202605010001', 990001, 990004, now(), '0', 5.00, '向广州前置仓补充热销机型库存', 'admin', now()
from dual where not exists (select 1 from wms_transfer where transfer_id = 1001);

insert into wms_transfer_item (transfer_item_id, transfer_id, product_id, quantity, remark)
select 1001, 1001, 990011, 5.00, '调拨 iPhone 15 Pro 5台'
from dual where not exists (select 1 from wms_transfer_item where transfer_item_id = 1001);

-- 9. 替换通知公告中的若依旧文案与乱码内容为 SaleERP 业务公告
update sys_notice
set notice_title = '系统通知：SaleERP 统一待办中心与全链路追踪已上线',
    notice_type = '2',
    notice_content = convert('各部门同事请注意：系统已正式启用“统一待办中心”及采购/销售“链路追踪”视图，请及时处理名下待审单据与库存预警事项。' using utf8mb4),
    update_by = 'admin',
    update_time = now()
where notice_id = 1;

update sys_notice
set notice_title = '经营提醒：月末仓库盘点与逾期应收账款集中核对',
    notice_type = '1',
    notice_content = convert('请仓储部于每月最后一个工作日前完成重点库位盘点，销售部协同财务部在“应收台账”与“客户跟进”中跟进逾期账款回款进度。' using utf8mb4),
    update_by = 'admin',
    update_time = now()
where notice_id = 2;
