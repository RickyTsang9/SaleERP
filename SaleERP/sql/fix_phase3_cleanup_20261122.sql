-- SaleERP 第三阶段收口清理脚本：修复残留乱码、孤立菜单节点、清理若依演示数据与补齐管理驾驶舱样例 (2026-11-22)

-- 1. 修复 sys_config 乱码备注（不改动开关值）
update sys_config
set remark = '开发阶段默认关闭验证码功能（true开启，false关闭）',
    update_by = 'admin',
    update_time = now()
where config_id = 4 and config_key = 'sys.account.captchaEnabled';

-- 2. 修复 sys_menu 中 6 个孤立/乱码按钮权限节点，正确挂载至现有业务菜单下
update sys_menu set menu_name = '核销确认', parent_id = 3017, menu_type = 'F', visible = '0', status = '0' where menu_id = 2364;
update sys_menu set menu_name = '业务链路查询', parent_id = 3013, menu_type = 'F', visible = '0', status = '0' where menu_id = 2470;
update sys_menu set menu_name = '商品导入', parent_id = 3001, menu_type = 'F', visible = '0', status = '0' where menu_id = 2471;
update sys_menu set menu_name = '客户导入', parent_id = 3002, menu_type = 'F', visible = '0', status = '0' where menu_id = 2472;
update sys_menu set menu_name = '供应商导入', parent_id = 5205, menu_type = 'F', visible = '0', status = '0' where menu_id = 2473;
update sys_menu set menu_name = '库存预警阈值设置', parent_id = 3009, perms = 'business:stock:edit', menu_type = 'F', visible = '0', status = '0' where menu_id = 2474;

insert into sys_role_menu(role_id, menu_id)
select 2, m.menu_id
from sys_menu m
where m.menu_id in (2364, 2470, 2471, 2472, 2473, 2474)
  and not exists (
    select 1 from sys_role_menu rm where rm.role_id = 2 and rm.menu_id = m.menu_id
  );

-- 3. 清理若依自带示例定时任务与演示测试用户名
delete from sys_job where job_id in (1, 2, 3) and invoke_target like 'ryTask.%';

update sys_user
set user_name = 'tester',
    nick_name = '系统测试员',
    update_by = 'admin',
    update_time = now()
where user_id = 2 and user_name = 'ry';

-- 4. 补齐管理层驾驶舱（年度预算计划、经营简报归档、经营快照、经营决议事项）演示数据
insert into biz_decision_budget_plan (
    plan_id, budget_year, plan_name, version_no, version_label, effective_flag, plan_status,
    sale_budget_amount, gross_profit_budget_amount, collection_budget_amount, purchase_budget_amount, net_cash_budget_amount,
    sale_monthly_plan_text, gross_profit_monthly_plan_text, collection_monthly_plan_text, purchase_monthly_plan_text, net_cash_monthly_plan_text,
    submit_by, submit_time, approve_by, approve_time, approve_remark, remark, create_by, create_time
)
select
    1, 2026, '2026年度销售与资金经营预算（V1基线）', 1, 'V1', 'y', 'approved',
    4800000.00, 960000.00, 4200000.00, 3200000.00, 1000000.00,
    '[350000,350000,380000,400000,400000,420000,400000,400000,420000,420000,430000,430000]',
    '[70000,70000,76000,80000,80000,84000,80000,80000,84000,84000,86000,86000]',
    '[300000,300000,330000,350000,350000,370000,350000,350000,370000,370000,380000,380000]',
    '[240000,240000,260000,270000,270000,280000,260000,260000,280000,280000,280000,280000]',
    '[60000,60000,70000,80000,80000,90000,90000,90000,90000,90000,100000,100000]',
    'admin', now(), 'admin', now(), '年度经营预算评审通过并生效', '2026年度正式基线预算', 'admin', now()
from dual where not exists (select 1 from biz_decision_budget_plan where plan_id = 1);

insert into biz_executive_brief_record (
    brief_id, brief_title, brief_date, brief_status, generated_time,
    summary_content, highlight_content, risk_content, action_content, plain_text_content,
    source_mode, remark, create_by, create_time
)
select
    1, 'SaleERP 阶段经营复盘简报', curdate(), 'archived', now(),
    '当前累计已审销售总额 ￥340,447.00，已收款 ￥89,990.00，整体毛利结构健康，但逾期应收与重点热销机型低库存需要立即协同跟进。',
    '["头部客户北京华数科技与上海天创商贸贡献稳定复购","手机通讯与电脑办公双品类毛利率保持良好水平"]',
    '["广州智联网络科技存在 ￥71,992.00 逾期应收待催收","华为 Mate 60 Pro 与 MacBook Air M2 库存低于安全下限"]',
    '["销售与财务联动于本周内落实逾期回款计划","采购部加速推进 PO20231024001/002 审核与入库上架"]',
    '【SaleERP 阶段经营复盘简报】\n一、经营概览：累计已审销售 ￥340,447.00，应收 ￥186,977.00，已收 ￥89,990.00。\n二、核心风险：1) 逾期应收 ￥71,992.00；2) 2项热销商品低于安全库存下限。\n三、决议建议：优先跟进催收并加快采购入库。',
    'dashboard', '系统初始化示例简报', 'admin', now()
from dual where not exists (select 1 from biz_executive_brief_record where brief_id = 1);

insert into biz_operation_snapshot (
    snapshot_id, snapshot_title, snapshot_date, snapshot_type,
    sale_amount, gross_profit_amount, collection_rate, overdue_count, stock_warning_count, pending_action_count,
    summary_content, plain_text_content, source_mode, remark, create_by, create_time
)
select
    1, '2026阶段经营基线快照', curdate(), 'weekly',
    340447.00, 46447.00, 48.13, 1, 3, 2,
    '阶段基线经营快照：销售稳步推进，重点控制应收逾期与安全库存水位。',
    '快照日期：当前周期 | 销售额：￥340,447.00 | 回款率：48.13% | 逾期应收：1笔 | 库存预警：3项',
    'dashboard', '阶段对比基线', 'admin', now()
from dual where not exists (select 1 from biz_operation_snapshot where snapshot_id = 1);

insert into biz_executive_action_item (
    action_item_id, brief_id, brief_title_snapshot, action_title, owner_name,
    due_date, action_status, priority_level, progress_remark, remark, create_by, create_time
)
select
    1, 1, 'SaleERP 阶段经营复盘简报', '专项跟进广州智联网络科技 ￥71,992.00 逾期应收回款', '张经理',
    date_add(curdate(), interval 3 day), 'in_progress', 'high', '已电话联系客户财务，预计本周五前安排对公转账', '销售与财务联合跟进', 'admin', now()
from dual where not exists (select 1 from biz_executive_action_item where action_item_id = 1);

insert into biz_executive_action_item (
    action_item_id, brief_id, brief_title_snapshot, action_title, owner_name,
    due_date, action_status, priority_level, progress_remark, remark, create_by, create_time
)
select
    2, 1, 'SaleERP 阶段经营复盘简报', '推进华为 Mate 60 Pro 及 MacBook Air M2 采购入库补货', '陈主管',
    date_add(curdate(), interval 5 day), 'todo', 'medium', '待采购订单审核通过后安排华南一号仓/二号仓收货上架', '保障热销机型不断货', 'admin', now()
from dual where not exists (select 1 from biz_executive_action_item where action_item_id = 2);
