create table if not exists biz_decision_budget_plan (
    plan_id bigint(20) not null auto_increment comment '预算计划id',
    budget_year int(4) not null comment '预算年度',
    plan_name varchar(100) not null comment '预算计划名称',
    version_no int(4) not null default 1 comment '版本号',
    version_label varchar(50) default 'v1' comment '版本标签',
    base_plan_id bigint(20) default null comment '基线计划id',
    effective_flag char(1) not null default 'n' comment '是否为当前生效版本',
    adjustment_reason varchar(500) default null comment '调整原因',
    plan_status varchar(20) not null default 'draft' comment '计划状态',
    sale_budget_amount decimal(16, 2) default 0.00 comment '年度销售预算',
    gross_profit_budget_amount decimal(16, 2) default 0.00 comment '年度毛利预算',
    collection_budget_amount decimal(16, 2) default 0.00 comment '年度回款预算',
    purchase_budget_amount decimal(16, 2) default 0.00 comment '年度采购预算',
    net_cash_budget_amount decimal(16, 2) default 0.00 comment '年度净现金流预算',
    sale_monthly_plan_text text comment '销售月度预算json',
    gross_profit_monthly_plan_text text comment '毛利月度预算json',
    collection_monthly_plan_text text comment '回款月度预算json',
    purchase_monthly_plan_text text comment '采购月度预算json',
    net_cash_monthly_plan_text text comment '净现金流月度预算json',
    submit_by varchar(64) default '' comment '提交人',
    submit_time datetime default null comment '提交时间',
    approve_by varchar(64) default '' comment '审批人',
    approve_time datetime default null comment '审批时间',
    approve_remark varchar(500) default null comment '审批意见',
    remark varchar(500) default null comment '备注',
    create_by varchar(64) default '' comment '创建者',
    create_time datetime default null comment '创建时间',
    update_by varchar(64) default '' comment '更新者',
    update_time datetime default null comment '更新时间',
    primary key (plan_id),
    key idx_budget_year_version_no (budget_year, version_no),
    key idx_budget_year_effective_flag (budget_year, effective_flag)
) engine=innodb auto_increment=1 comment='管理层年度预算计划';

create table if not exists biz_executive_brief_record (
    brief_id bigint(20) not null auto_increment comment '简报id',
    brief_title varchar(100) not null comment '简报标题',
    brief_date datetime default null comment '简报日期',
    brief_status varchar(20) not null default 'archived' comment '简报状态',
    generated_time datetime default null comment '生成时间',
    summary_content text comment '经营结论',
    highlight_content text comment '亮点内容json',
    risk_content text comment '风险内容json',
    action_content text comment '建议决策json',
    plain_text_content longtext comment '纯文本简报',
    source_mode varchar(20) default 'dashboard' comment '生成来源',
    remark varchar(500) default null comment '备注',
    create_by varchar(64) default '' comment '创建者',
    create_time datetime default null comment '创建时间',
    update_by varchar(64) default '' comment '更新者',
    update_time datetime default null comment '更新时间',
    primary key (brief_id),
    key idx_brief_date (brief_date)
) engine=innodb auto_increment=1 comment='管理层经营简报归档';

create table if not exists biz_executive_action_item (
    action_item_id bigint(20) not null auto_increment comment '事项id',
    brief_id bigint(20) default null comment '简报id',
    brief_title_snapshot varchar(100) default null comment '简报标题快照',
    action_title varchar(255) not null comment '决议事项',
    owner_name varchar(64) default '' comment '负责人',
    due_date datetime default null comment '到期日期',
    action_status varchar(20) not null default 'todo' comment '事项状态',
    priority_level varchar(20) not null default 'medium' comment '优先级',
    progress_remark varchar(500) default null comment '进度备注',
    completed_time datetime default null comment '完成时间',
    remark varchar(500) default null comment '备注',
    create_by varchar(64) default '' comment '创建者',
    create_time datetime default null comment '创建时间',
    update_by varchar(64) default '' comment '更新者',
    update_time datetime default null comment '更新时间',
    primary key (action_item_id),
    key idx_action_status_due_date (action_status, due_date),
    key idx_brief_id (brief_id)
) engine=innodb auto_increment=1 comment='管理层经营决议事项';

create table if not exists biz_operation_snapshot (
    snapshot_id bigint(20) not null auto_increment comment '快照id',
    snapshot_title varchar(100) not null comment '快照标题',
    snapshot_date datetime default null comment '快照日期',
    snapshot_type varchar(20) not null default 'weekly' comment '快照类型',
    sale_amount decimal(16, 2) default 0.00 comment '销售额',
    gross_profit_amount decimal(16, 2) default 0.00 comment '毛利额',
    collection_rate decimal(10, 2) default 0.00 comment '回款率',
    overdue_count int(11) default 0 comment '逾期笔数',
    stock_warning_count int(11) default 0 comment '库存预警数',
    pending_action_count int(11) default 0 comment '待办事项数',
    summary_content text comment '经营结论',
    plain_text_content longtext comment '快照纯文本',
    source_mode varchar(20) default 'dashboard' comment '生成来源',
    remark varchar(500) default null comment '备注',
    create_by varchar(64) default '' comment '创建者',
    create_time datetime default null comment '创建时间',
    update_by varchar(64) default '' comment '更新者',
    update_time datetime default null comment '更新时间',
    primary key (snapshot_id),
    key idx_snapshot_date (snapshot_date),
    key idx_snapshot_type (snapshot_type)
) engine=innodb auto_increment=1 comment='管理层经营快照';

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 4902, '看板编辑', 3019, 2, '#', '', '', '', 1, 0, 'f', '0', '0', 'business:report:edit', '#', 'admin', sysdate(), '', null, '经营看板编辑权限'
from dual
where not exists (
    select 1 from sys_menu where menu_id = 4902
);

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 4903, '看板审批', 3019, 3, '#', '', '', '', 1, 0, 'f', '0', '0', 'business:report:approve', '#', 'admin', sysdate(), '', null, '经营看板审批权限'
from dual
where not exists (
    select 1 from sys_menu where menu_id = 4903
);

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 4904, '简报归档', 3019, 4, '#', '', '', '', 1, 0, 'f', '0', '0', 'business:report:brief', '#', 'admin', sysdate(), '', null, '经营简报归档权限'
from dual
where not exists (
    select 1 from sys_menu where menu_id = 4904
);
