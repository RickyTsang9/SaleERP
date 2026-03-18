-- 新增业务表建表语句与样例数据
-- 来源：ry_20250522.sql 中业务模块新增表

set @md_customer_table_exists := (
  select count(*)
  from information_schema.tables
  where table_schema = database()
    and table_name = 'md_customer'
);
set @sql_text := if(
  @md_customer_table_exists = 0,
  'create table md_customer (
    customer_id bigint(20) not null auto_increment comment ''客户主键'',
    customer_code varchar(64) not null comment ''客户编码'',
    customer_name varchar(128) not null comment ''客户名称'',
    contact_person varchar(64) default null comment ''联系人'',
    contact_phone varchar(32) default null comment ''联系电话'',
    contact_email varchar(128) default null comment ''邮箱'',
    address varchar(255) default null comment ''地址'',
    credit_limit decimal(18,2) default 0 comment ''信用额度'',
    payment_method varchar(30) default ''cash'' comment ''付款方式'',
    customer_level varchar(30) default ''normal'' comment ''客户等级'',
    status char(1) default ''0'' comment ''状态'',
    del_flag char(1) default ''0'' comment ''删除标记'',
    create_by varchar(64) default '''' comment ''创建者'',
    create_time datetime comment ''创建时间'',
    update_by varchar(64) default '''' comment ''更新者'',
    update_time datetime comment ''更新时间'',
    remark varchar(255) default null comment ''备注'',
    primary key (customer_id),
    unique key uk_md_customer_code (customer_code)
  ) engine=innodb auto_increment=1 comment=''客户资料表''',
  'select 1'
);
prepare md_customer_create_table_stmt from @sql_text;
execute md_customer_create_table_stmt;
deallocate prepare md_customer_create_table_stmt;

set @md_customer_table_exists := (
  select count(*)
  from information_schema.tables
  where table_schema = database()
    and table_name = 'md_customer'
);

set @md_customer_credit_limit_exists := (
  select count(*)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'md_customer'
    and column_name = 'credit_limit'
);
set @sql_text := if(
  @md_customer_table_exists = 1 and @md_customer_credit_limit_exists = 0,
  'alter table md_customer add column credit_limit decimal(18,2) default 0 comment ''信用额度''',
  'select 1'
);
prepare md_customer_credit_limit_stmt from @sql_text;
execute md_customer_credit_limit_stmt;
deallocate prepare md_customer_credit_limit_stmt;

set @md_customer_payment_method_exists := (
  select count(*)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'md_customer'
    and column_name = 'payment_method'
);
set @sql_text := if(
  @md_customer_table_exists = 1 and @md_customer_payment_method_exists = 0,
  'alter table md_customer add column payment_method varchar(30) default ''cash'' comment ''付款方式''',
  'select 1'
);
prepare md_customer_payment_method_stmt from @sql_text;
execute md_customer_payment_method_stmt;
deallocate prepare md_customer_payment_method_stmt;

set @md_customer_customer_level_exists := (
  select count(*)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'md_customer'
    and column_name = 'customer_level'
);
set @sql_text := if(
  @md_customer_table_exists = 1 and @md_customer_customer_level_exists = 0,
  'alter table md_customer add column customer_level varchar(30) default ''normal'' comment ''客户等级''',
  'select 1'
);
prepare md_customer_customer_level_stmt from @sql_text;
execute md_customer_customer_level_stmt;
deallocate prepare md_customer_customer_level_stmt;

drop table if exists wms_sale_order;
create table wms_sale_order (
  sale_order_id      bigint(20)      not null auto_increment comment '销售单主键',
  order_no           varchar(32)     not null                comment '销售单号',
  customer_id        bigint(20)      not null                comment '客户编号',
  warehouse_id       bigint(20)      not null                comment '仓库编号',
  total_qty          decimal(18,2)   default 0               comment '总数量',
  total_amount       decimal(18,2)   default 0               comment '总金额',
  status             varchar(20)     default 'draft'         comment '状态',
  payment_status     varchar(20)     default 'unpaid'        comment '付款状态',
  manager_audit_by   varchar(64)     default null            comment '经理审核人',
  manager_audit_time datetime                                  comment '经理审核时间',
  manager_audit_comment varchar(500) default null            comment '经理审核意见',
  audit_by           varchar(64)     default null            comment '审核人',
  audit_time         datetime                                  comment '审核时间',
  finance_audit_comment varchar(500) default null            comment '财务审核意见',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (sale_order_id),
  unique key uk_wms_sale_order_no (order_no)
) engine=innodb auto_increment=1 comment='销售订单主表';

drop table if exists wms_sale_order_item;
create table wms_sale_order_item (
  sale_order_item_id bigint(20)      not null auto_increment comment '销售明细主键',
  sale_order_id      bigint(20)      not null                comment '销售单编号',
  product_id         bigint(20)      not null                comment '商品编号',
  location_id        bigint(20)      default null            comment '库位编号',
  batch_no           varchar(64)     default null            comment '批次号',
  quantity           decimal(18,2)   default 0               comment '数量',
  price              decimal(18,2)   default 0               comment '单价',
  amount             decimal(18,2)   default 0               comment '金额',
  primary key (sale_order_item_id),
  key idx_wms_sale_order_item_order_id (sale_order_id)
) engine=innodb auto_increment=1 comment='销售订单明细表';

drop table if exists wms_sale_order_status_history;
create table wms_sale_order_status_history (
  history_id         bigint(20)      not null auto_increment comment '状态历史主键',
  sale_order_id      bigint(20)      not null                comment '销售单编号',
  from_status        varchar(20)     default null            comment '原状态',
  to_status          varchar(20)     default null            comment '新状态',
  operation_type     varchar(50)     default null            comment '操作类型',
  audit_role         varchar(30)     default null            comment '审核角色',
  audit_by           varchar(64)     default null            comment '操作人',
  audit_comment      varchar(500)    default null            comment '审核意见',
  operate_time       datetime                                  comment '操作时间',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (history_id),
  key idx_wms_sale_order_status_history_order_id (sale_order_id)
) engine=innodb auto_increment=1 comment='销售订单状态历史表';

drop table if exists fin_receivable;
create table fin_receivable (
  receivable_id      bigint(20)      not null auto_increment comment '应收主键',
  sale_order_id      bigint(20)      not null                comment '销售单编号',
  customer_id        bigint(20)      not null                comment '客户编号',
  amount_due         decimal(18,2)   default 0               comment '应收金额',
  amount_paid        decimal(18,2)   default 0               comment '已收金额',
  status             varchar(20)     default 'unpaid'        comment '状态',
  due_date           datetime                                  comment '到期日期',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (receivable_id),
  unique key uk_fin_receivable_sale_order_id (sale_order_id)
) engine=innodb auto_increment=1 comment='应收台账表';

drop table if exists fin_receipt;
create table fin_receipt (
  receipt_id         bigint(20)      not null auto_increment comment '回款主键',
  receivable_id      bigint(20)      not null                comment '应收编号',
  sale_order_id      bigint(20)      not null                comment '销售单编号',
  customer_id        bigint(20)      not null                comment '客户编号',
  amount             decimal(18,2)   default 0               comment '回款金额',
  receipt_date       datetime                                  comment '回款日期',
  remark             varchar(255)    default null            comment '备注',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  primary key (receipt_id),
  key idx_fin_receipt_receivable_id (receivable_id)
) engine=innodb auto_increment=1 comment='回款登记表';

drop table if exists md_warehouse;
create table md_warehouse (
  warehouse_id       bigint(20)      not null auto_increment comment '仓库主键',
  warehouse_code     varchar(64)     not null                comment '仓库编码',
  warehouse_name     varchar(128)    not null                comment '仓库名称',
  contact_name       varchar(64)     default null            comment '联系人',
  contact_phone      varchar(32)     default null            comment '联系电话',
  address            varchar(255)    default null            comment '地址',
  status             char(1)         default '0'             comment '状态',
  del_flag           char(1)         default '0'             comment '删除标记',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (warehouse_id),
  unique key uk_md_warehouse_code (warehouse_code)
) engine=innodb auto_increment=1 comment='仓库资料表';

drop table if exists md_location;
create table md_location (
  location_id        bigint(20)      not null auto_increment comment '库位主键',
  warehouse_id       bigint(20)      not null                comment '仓库编号',
  location_code      varchar(64)     not null                comment '库位编码',
  location_name      varchar(128)    not null                comment '库位名称',
  status             char(1)         default '0'             comment '状态',
  del_flag           char(1)         default '0'             comment '删除标记',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (location_id),
  unique key uk_md_location_code (location_code)
) engine=innodb auto_increment=1 comment='库位资料表';

drop table if exists wms_sale_return;
create table wms_sale_return (
  sale_return_id     bigint(20)      not null auto_increment comment '销售退货主键',
  return_no          varchar(32)     not null                comment '退货单号',
  return_type        varchar(20)     default null            comment '退货类型',
  customer_id        bigint(20)      not null                comment '客户编号',
  warehouse_id       bigint(20)      not null                comment '仓库编号',
  total_qty          decimal(18,2)   default 0               comment '总数量',
  total_amount       decimal(18,2)   default 0               comment '总金额',
  status             varchar(20)     default 'draft'         comment '状态',
  audit_by           varchar(64)     default null            comment '审核人',
  audit_time         datetime                                  comment '审核时间',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (sale_return_id),
  unique key uk_wms_sale_return_no (return_no)
) engine=innodb auto_increment=1 comment='销售退货主表';

drop table if exists wms_sale_return_item;
create table wms_sale_return_item (
  sale_return_item_id bigint(20)      not null auto_increment comment '销售退货明细主键',
  sale_return_id      bigint(20)      not null                comment '销售退货编号',
  product_id          bigint(20)      not null                comment '商品编号',
  quantity            decimal(18,2)   default 0               comment '数量',
  price               decimal(18,2)   default 0               comment '单价',
  amount              decimal(18,2)   default 0               comment '金额',
  primary key (sale_return_item_id),
  key idx_wms_sale_return_item_return_id (sale_return_id)
) engine=innodb auto_increment=1 comment='销售退货明细表';

drop table if exists wms_inventory_check;
create table wms_inventory_check (
  check_id            bigint(20)      not null auto_increment comment '库存盘点主键',
  check_no            varchar(32)     not null                comment '盘点单号',
  warehouse_id        bigint(20)      not null                comment '仓库编号',
  status              varchar(20)     default 'draft'         comment '状态',
  total_diff_qty      decimal(18,2)   default 0               comment '差异数量',
  total_diff_amount   decimal(18,2)   default 0               comment '差异金额',
  audit_by            varchar(64)     default null            comment '审核人',
  audit_time          datetime                                  comment '审核时间',
  create_by           varchar(64)     default ''              comment '创建者',
  create_time         datetime                                  comment '创建时间',
  update_by           varchar(64)     default ''              comment '更新者',
  update_time         datetime                                  comment '更新时间',
  remark              varchar(255)    default null            comment '备注',
  primary key (check_id),
  unique key uk_wms_inventory_check_no (check_no)
) engine=innodb auto_increment=1 comment='库存盘点主表';

drop table if exists wms_inventory_check_item;
create table wms_inventory_check_item (
  check_item_id       bigint(20)      not null auto_increment comment '库存盘点明细主键',
  check_id            bigint(20)      not null                comment '盘点单编号',
  product_id          bigint(20)      not null                comment '商品编号',
  location_id         bigint(20)      default null            comment '库位编号',
  batch_no            varchar(64)     default null            comment '批次号',
  stock_qty           decimal(18,2)   default 0               comment '账面数量',
  actual_qty          decimal(18,2)   default 0               comment '实盘数量',
  diff_qty            decimal(18,2)   default 0               comment '差异数量',
  price               decimal(18,2)   default 0               comment '单价',
  diff_amount         decimal(18,2)   default 0               comment '差异金额',
  primary key (check_item_id),
  key idx_wms_inventory_check_item_check_id (check_id)
) engine=innodb auto_increment=1 comment='库存盘点明细表';

-- 样例数据（按依赖顺序）
delete from wms_inventory_check_item where check_item_id in (900001, 900002);
delete from wms_inventory_check where check_id in (900001);
delete from fin_receipt where receipt_id in (900001);
delete from fin_receivable where receivable_id in (900001);
delete from wms_sale_return_item where sale_return_item_id in (900001, 900002);
delete from wms_sale_return where sale_return_id in (900001);
delete from wms_sale_order_item where sale_order_item_id in (900001, 900002);
delete from wms_sale_order where sale_order_id in (900001);
delete from md_location where location_id in (900001, 900002);
delete from md_warehouse where warehouse_id in (900001);

insert into md_warehouse
(warehouse_id, warehouse_code, warehouse_name, contact_name, contact_phone, address, status, del_flag, create_by, create_time, update_by, update_time, remark)
values
(900001, 'wh_sz_01', '深圳一号仓', '张三', '13800138000', '深圳市南山区xx路1号', '0', '0', 'admin', now(), 'admin', now(), '样例仓库');

insert into md_location
(location_id, warehouse_id, location_code, location_name, status, del_flag, create_by, create_time, update_by, update_time, remark)
values
(900001, 900001, 'loc_a_01', 'a区01货位', '0', '0', 'admin', now(), 'admin', now(), '样例库位1'),
(900002, 900001, 'loc_a_02', 'a区02货位', '0', '0', 'admin', now(), 'admin', now(), '样例库位2');

insert into wms_sale_order
(sale_order_id, order_no, customer_id, warehouse_id, total_qty, total_amount, status, payment_status, manager_audit_by, manager_audit_time, audit_by, audit_time, create_by, create_time, update_by, update_time, remark)
values
(900001, 'xs202603130001', 1, 900001, 15.00, 1500.00, 'audited', 'part_paid', 'manager_user', now(), 'admin', now(), 'admin', now(), 'admin', now(), '样例销售订单');

insert into wms_sale_order_item
(sale_order_item_id, sale_order_id, product_id, location_id, batch_no, quantity, price, amount)
values
(900001, 900001, 1, 900001, 'b20260313a', 10.00, 100.00, 1000.00),
(900002, 900001, 2, 900002, 'b20260313b', 5.00, 100.00, 500.00);

insert into fin_receivable
(receivable_id, sale_order_id, customer_id, amount_due, amount_paid, status, due_date, create_by, create_time, update_by, update_time, remark)
values
(900001, 900001, 1, 1500.00, 500.00, 'partial', date_add(now(), interval 15 day), 'admin', now(), 'admin', now(), '样例应收');

insert into fin_receipt
(receipt_id, receivable_id, sale_order_id, customer_id, amount, receipt_date, remark, create_by, create_time, update_by, update_time)
values
(900001, 900001, 900001, 1, 500.00, now(), '首笔回款样例', 'admin', now(), 'admin', now());

insert into wms_sale_return
(sale_return_id, return_no, return_type, customer_id, warehouse_id, total_qty, total_amount, status, audit_by, audit_time, create_by, create_time, update_by, update_time, remark)
values
(900001, 'xt202603130001', 'quality', 1, 900001, 2.00, 200.00, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), '样例销售退货');

insert into wms_sale_return_item
(sale_return_item_id, sale_return_id, product_id, quantity, price, amount)
values
(900001, 900001, 1, 1.00, 100.00, 100.00),
(900002, 900001, 2, 1.00, 100.00, 100.00);

insert into wms_inventory_check
(check_id, check_no, warehouse_id, status, total_diff_qty, total_diff_amount, audit_by, audit_time, create_by, create_time, update_by, update_time, remark)
values
(900001, 'pd202603130001', 900001, 'draft', 0.00, 0.00, null, null, 'admin', now(), 'admin', now(), '样例库存盘点');

insert into wms_inventory_check_item
(check_item_id, check_id, product_id, location_id, batch_no, stock_qty, actual_qty, diff_qty, price, diff_amount)
values
(900001, 900001, 1, 900001, 'b20260313a', 10.00, 9.00, -1.00, 100.00, -100.00),
(900002, 900001, 2, 900002, 'b20260313b', 5.00, 6.00, 1.00, 100.00, 100.00);

-- 销售审核闭环增量迁移脚本（可重复执行）
set @sale_order_table_exists := (
  select count(*)
  from information_schema.tables
  where table_schema = database()
    and table_name = 'wms_sale_order'
);
set @sql_text := if(
  @sale_order_table_exists = 0,
  'create table wms_sale_order (
    sale_order_id bigint(20) not null auto_increment comment ''销售单主键'',
    order_no varchar(32) not null comment ''销售单号'',
    customer_id bigint(20) not null comment ''客户编号'',
    warehouse_id bigint(20) not null comment ''仓库编号'',
    total_qty decimal(18,2) default 0 comment ''总数量'',
    total_amount decimal(18,2) default 0 comment ''总金额'',
    status varchar(20) default ''draft'' comment ''状态'',
    payment_status varchar(20) default ''unpaid'' comment ''付款状态'',
    manager_audit_by varchar(64) default null comment ''经理审核人'',
    manager_audit_time datetime comment ''经理审核时间'',
    manager_audit_comment varchar(500) default null comment ''经理审核意见'',
    audit_by varchar(64) default null comment ''审核人'',
    audit_time datetime comment ''审核时间'',
    finance_audit_comment varchar(500) default null comment ''财务审核意见'',
    create_by varchar(64) default '''' comment ''创建者'',
    create_time datetime comment ''创建时间'',
    update_by varchar(64) default '''' comment ''更新者'',
    update_time datetime comment ''更新时间'',
    remark varchar(255) default null comment ''备注'',
    primary key (sale_order_id),
    unique key uk_wms_sale_order_no (order_no)
  ) engine=innodb auto_increment=1 comment=''销售订单主表''',
  'select 1'
);
prepare sale_order_table_stmt from @sql_text;
execute sale_order_table_stmt;
deallocate prepare sale_order_table_stmt;

set @manager_audit_comment_exists := (
  select count(*)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'wms_sale_order'
    and column_name = 'manager_audit_comment'
);
set @sql_text := if(
  @manager_audit_comment_exists = 0,
  'alter table wms_sale_order add column manager_audit_comment varchar(500) default null comment ''经理审核意见'' after manager_audit_time',
  'select 1'
);
prepare manager_audit_comment_stmt from @sql_text;
execute manager_audit_comment_stmt;
deallocate prepare manager_audit_comment_stmt;

set @finance_audit_comment_exists := (
  select count(*)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'wms_sale_order'
    and column_name = 'finance_audit_comment'
);
set @sql_text := if(
  @finance_audit_comment_exists = 0,
  'alter table wms_sale_order add column finance_audit_comment varchar(500) default null comment ''财务审核意见'' after audit_time',
  'select 1'
);
prepare finance_audit_comment_stmt from @sql_text;
execute finance_audit_comment_stmt;
deallocate prepare finance_audit_comment_stmt;

set @status_history_exists := (
  select count(*)
  from information_schema.tables
  where table_schema = database()
    and table_name = 'wms_sale_order_status_history'
);
set @sql_text := if(
  @status_history_exists = 0,
  'create table wms_sale_order_status_history (
    history_id bigint(20) not null auto_increment comment ''状态历史主键'',
    sale_order_id bigint(20) not null comment ''销售单编号'',
    from_status varchar(20) default null comment ''原状态'',
    to_status varchar(20) default null comment ''新状态'',
    operation_type varchar(50) default null comment ''操作类型'',
    audit_role varchar(30) default null comment ''审核角色'',
    audit_by varchar(64) default null comment ''操作人'',
    audit_comment varchar(500) default null comment ''审核意见'',
    operate_time datetime comment ''操作时间'',
    create_by varchar(64) default '''' comment ''创建者'',
    create_time datetime comment ''创建时间'',
    update_by varchar(64) default '''' comment ''更新者'',
    update_time datetime comment ''更新时间'',
    remark varchar(255) default null comment ''备注'',
    primary key (history_id),
    key idx_wms_sale_order_status_history_order_id (sale_order_id)
  ) engine=innodb auto_increment=1 comment=''销售订单状态历史表''',
  'select 1'
);
prepare status_history_stmt from @sql_text;
execute status_history_stmt;
deallocate prepare status_history_stmt;

set @wms_stock_table_exists := (
  select count(*)
  from information_schema.tables
  where table_schema = database()
    and table_name = 'wms_stock'
);
set @locked_quantity_exists := (
  select count(*)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'wms_stock'
    and column_name = 'locked_quantity'
);
set @sql_text := if(
  @wms_stock_table_exists > 0 and @locked_quantity_exists = 0,
  'alter table wms_stock add column locked_quantity decimal(18,2) default 0 comment ''锁定数量'' after quantity',
  'select 1'
);
prepare locked_quantity_stmt from @sql_text;
execute locked_quantity_stmt;
deallocate prepare locked_quantity_stmt;

set @frozen_quantity_exists := (
  select count(*)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'wms_stock'
    and column_name = 'frozen_quantity'
);
set @sql_text := if(
  @wms_stock_table_exists > 0 and @frozen_quantity_exists = 0,
  'alter table wms_stock add column frozen_quantity decimal(18,2) default 0 comment ''冻结数量'' after locked_quantity',
  'select 1'
);
prepare frozen_quantity_stmt from @sql_text;
execute frozen_quantity_stmt;
deallocate prepare frozen_quantity_stmt;
