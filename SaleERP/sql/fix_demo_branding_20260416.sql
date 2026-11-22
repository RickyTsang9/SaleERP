-- saleerp 演示品牌数据修复脚本

update sys_dept
set dept_name = case when dept_id = 100 then 'SaleERP科技' else dept_name end,
    leader = 'SaleERP管理员',
    email = 'admin@saleerp.local',
    update_by = 'admin',
    update_time = sysdate()
where dept_id in (100, 101, 102, 103, 104, 105, 106, 107, 108, 109);

update sys_user
set nick_name = 'SaleERP管理员',
    email = 'admin@saleerp.local',
    update_by = 'admin',
    update_time = sysdate()
where user_id = 1;

update sys_user
set nick_name = '系统测试员',
    email = 'tester@saleerp.local',
    update_by = 'admin',
    update_time = sysdate()
where user_id = 2;
