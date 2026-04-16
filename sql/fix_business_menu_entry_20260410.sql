use sale_erp;

update sys_menu set parent_id = 3025, order_num = 1, path = 'purchaseOrder', component = 'business/purchaseOrder/index', perms = 'business:purchaseOrder:list', menu_name = '采购订单', remark = '采购订单菜单' where menu_id = 5211;
update sys_menu set order_num = 2 where menu_id = 3005;
update sys_menu set order_num = 3 where menu_id = 3011;

update sys_menu set parent_id = 3028, order_num = 3, path = 'payable', component = 'business/payable/index', perms = 'business:payable:list', menu_name = '应付台账', remark = '应付台账菜单' where menu_id = 5218;
update sys_menu set menu_name = '应付台账查询', remark = '' where menu_id = 5219;
update sys_menu set menu_name = '应付台账新增', remark = '' where menu_id = 5220;
update sys_menu set menu_name = '应付台账修改', remark = '' where menu_id = 5221;
update sys_menu set menu_name = '应付台账删除', remark = '' where menu_id = 5222;
update sys_menu set menu_name = '应付台账导出', remark = '' where menu_id = 5223;

update sys_menu set parent_id = 3028, order_num = 4, path = 'payment', component = 'business/payment/index', perms = 'business:payment:list', menu_name = '付款流水', remark = '付款流水菜单' where menu_id = 5224;
update sys_menu set menu_name = '付款流水查询', remark = '' where menu_id = 5225;
update sys_menu set menu_name = '付款流水新增', remark = '' where menu_id = 5226;
update sys_menu set menu_name = '付款流水修改', remark = '' where menu_id = 5227;
update sys_menu set menu_name = '付款流水删除', remark = '' where menu_id = 5228;
update sys_menu set menu_name = '付款流水导出', remark = '' where menu_id = 5229;

update sys_menu set order_num = 5 where menu_id = 3019;
update sys_menu set order_num = 6 where menu_id = 3022;
