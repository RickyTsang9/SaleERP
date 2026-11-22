-- 新增统一待办中心菜单与权限
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5300, '统一待办中心', 3028, 0, 'todoCenter', 'business/todoCenter/index', null, '', 1, 0, 'c', '0', '0', 'business:todoCenter:list', 'todo', 'admin', sysdate(), '', null, '统一待办中心菜单'
from dual
where not exists (select 1 from sys_menu where menu_id = 5300);

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select 5301, '待办中心查询', 5300, 1, '#', '', null, '', 1, 0, 'f', '0', '0', 'business:todoCenter:list', '#', 'admin', sysdate(), '', null, ''
from dual
where not exists (select 1 from sys_menu where menu_id = 5301);
