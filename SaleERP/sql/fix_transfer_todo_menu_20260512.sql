-- 修复库存调拨与统一待办中心菜单入口

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5230, '库存调拨', 3027, 4, 'transfer', 'business/transfer/index', null, '', 1, 0, 'c', '0', '0', 'business:transfer:list', 'drag', 'admin', sysdate(), '', null, '库存调拨菜单'
from dual
where not exists (select 1 from sys_menu where menu_id = 5230);

update sys_menu
set menu_name = '库存调拨',
    parent_id = 3027,
    order_num = 4,
    path = 'transfer',
    component = 'business/transfer/index',
    query = null,
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'c',
    visible = '0',
    status = '0',
    perms = 'business:transfer:list',
    icon = 'drag',
    remark = '库存调拨菜单'
where menu_id = 5230;

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5231, '调拨单查询', 5230, 1, '#', '', null, '', 1, 0, 'f', '0', '0', 'business:transfer:query', '#', 'admin', sysdate(), '', null, ''
from dual
where not exists (select 1 from sys_menu where menu_id = 5231);

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5232, '调拨单新增', 5230, 2, '#', '', null, '', 1, 0, 'f', '0', '0', 'business:transfer:add', '#', 'admin', sysdate(), '', null, ''
from dual
where not exists (select 1 from sys_menu where menu_id = 5232);

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5233, '调拨单修改', 5230, 3, '#', '', null, '', 1, 0, 'f', '0', '0', 'business:transfer:edit', '#', 'admin', sysdate(), '', null, ''
from dual
where not exists (select 1 from sys_menu where menu_id = 5233);

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5234, '调拨单删除', 5230, 4, '#', '', null, '', 1, 0, 'f', '0', '0', 'business:transfer:remove', '#', 'admin', sysdate(), '', null, ''
from dual
where not exists (select 1 from sys_menu where menu_id = 5234);

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5235, '调拨单审核', 5230, 5, '#', '', null, '', 1, 0, 'f', '0', '0', 'business:transfer:audit', '#', 'admin', sysdate(), '', null, ''
from dual
where not exists (select 1 from sys_menu where menu_id = 5235);

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5236, '调拨单导出', 5230, 6, '#', '', null, '', 1, 0, 'f', '0', '0', 'business:transfer:export', '#', 'admin', sysdate(), '', null, ''
from dual
where not exists (select 1 from sys_menu where menu_id = 5236);

update sys_menu set parent_id = 5230, menu_type = 'f', visible = '0', status = '0' where menu_id in (5231, 5232, 5233, 5234, 5235, 5236);

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5300, '统一待办中心', 3028, 0, 'todoCenter', 'business/todoCenter/index', null, '', 1, 0, 'c', '0', '0', 'business:todoCenter:list', 'todo', 'admin', sysdate(), '', null, '统一待办中心菜单'
from dual
where not exists (select 1 from sys_menu where menu_id = 5300);

update sys_menu
set menu_name = '统一待办中心',
    parent_id = 3028,
    order_num = 0,
    path = 'todoCenter',
    component = 'business/todoCenter/index',
    query = null,
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'c',
    visible = '0',
    status = '0',
    perms = 'business:todoCenter:list',
    icon = 'todo',
    remark = '统一待办中心菜单'
where menu_id = 5300;

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5301, '待办中心查询', 5300, 1, '#', '', null, '', 1, 0, 'f', '0', '0', 'business:todoCenter:list', '#', 'admin', sysdate(), '', null, ''
from dual
where not exists (select 1 from sys_menu where menu_id = 5301);

update sys_menu set parent_id = 5300, menu_type = 'f', visible = '0', status = '0' where menu_id = 5301;

insert into sys_role_menu(role_id, menu_id)
select 2, menu_id
from sys_menu
where menu_id in (5230, 5231, 5232, 5233, 5234, 5235, 5236, 5300, 5301)
  and not exists (
    select 1
    from sys_role_menu
    where sys_role_menu.role_id = 2
      and sys_role_menu.menu_id = sys_menu.menu_id
  );
