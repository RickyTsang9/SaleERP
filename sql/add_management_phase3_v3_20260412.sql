set @budget_year_unique_index_count = (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'biz_decision_budget_plan'
      and index_name = 'uk_budget_year'
);
set @drop_budget_year_unique_index_sql = if(
    @budget_year_unique_index_count > 0,
    'alter table biz_decision_budget_plan drop index uk_budget_year',
    'select 1'
);
prepare budget_year_unique_index_statement from @drop_budget_year_unique_index_sql;
execute budget_year_unique_index_statement;
deallocate prepare budget_year_unique_index_statement;

set @version_no_column_count = (
    select count(1)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'biz_decision_budget_plan'
      and column_name = 'version_no'
);
set @add_version_no_column_sql = if(
    @version_no_column_count > 0,
    'select 1',
    'alter table biz_decision_budget_plan add column version_no int(4) not null default 1 comment ''版本号'' after plan_name'
);
prepare version_no_column_statement from @add_version_no_column_sql;
execute version_no_column_statement;
deallocate prepare version_no_column_statement;

set @version_label_column_count = (
    select count(1)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'biz_decision_budget_plan'
      and column_name = 'version_label'
);
set @add_version_label_column_sql = if(
    @version_label_column_count > 0,
    'select 1',
    'alter table biz_decision_budget_plan add column version_label varchar(50) default ''v1'' comment ''版本标签'' after version_no'
);
prepare version_label_column_statement from @add_version_label_column_sql;
execute version_label_column_statement;
deallocate prepare version_label_column_statement;

set @base_plan_id_column_count = (
    select count(1)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'biz_decision_budget_plan'
      and column_name = 'base_plan_id'
);
set @add_base_plan_id_column_sql = if(
    @base_plan_id_column_count > 0,
    'select 1',
    'alter table biz_decision_budget_plan add column base_plan_id bigint(20) default null comment ''基线计划id'' after version_label'
);
prepare base_plan_id_column_statement from @add_base_plan_id_column_sql;
execute base_plan_id_column_statement;
deallocate prepare base_plan_id_column_statement;

set @effective_flag_column_count = (
    select count(1)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'biz_decision_budget_plan'
      and column_name = 'effective_flag'
);
set @add_effective_flag_column_sql = if(
    @effective_flag_column_count > 0,
    'select 1',
    'alter table biz_decision_budget_plan add column effective_flag char(1) not null default ''n'' comment ''是否为当前生效版本'' after base_plan_id'
);
prepare effective_flag_column_statement from @add_effective_flag_column_sql;
execute effective_flag_column_statement;
deallocate prepare effective_flag_column_statement;

set @adjustment_reason_column_count = (
    select count(1)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'biz_decision_budget_plan'
      and column_name = 'adjustment_reason'
);
set @add_adjustment_reason_column_sql = if(
    @adjustment_reason_column_count > 0,
    'select 1',
    'alter table biz_decision_budget_plan add column adjustment_reason varchar(500) default null comment ''调整原因'' after effective_flag'
);
prepare adjustment_reason_column_statement from @add_adjustment_reason_column_sql;
execute adjustment_reason_column_statement;
deallocate prepare adjustment_reason_column_statement;

update biz_decision_budget_plan
set version_no = ifnull(version_no, 1),
    version_label = ifnull(nullif(version_label, ''), concat('v', ifnull(version_no, 1))),
    effective_flag = case
        when plan_status = 'approved' then 'y'
        else ifnull(nullif(effective_flag, ''), 'n')
    end
where 1 = 1;

set @budget_year_version_index_count = (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'biz_decision_budget_plan'
      and index_name = 'idx_budget_year_version_no'
);
set @create_budget_year_version_index_sql = if(
    @budget_year_version_index_count > 0,
    'select 1',
    'create index idx_budget_year_version_no on biz_decision_budget_plan (budget_year, version_no)'
);
prepare budget_year_version_index_statement from @create_budget_year_version_index_sql;
execute budget_year_version_index_statement;
deallocate prepare budget_year_version_index_statement;

set @budget_year_effective_index_count = (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'biz_decision_budget_plan'
      and index_name = 'idx_budget_year_effective_flag'
);
set @create_budget_year_effective_index_sql = if(
    @budget_year_effective_index_count > 0,
    'select 1',
    'create index idx_budget_year_effective_flag on biz_decision_budget_plan (budget_year, effective_flag)'
);
prepare budget_year_effective_index_statement from @create_budget_year_effective_index_sql;
execute budget_year_effective_index_statement;
deallocate prepare budget_year_effective_index_statement;

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
