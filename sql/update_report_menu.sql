USE sale_erp;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES ('销售明细报表', 2000, 10, 'saleReport', 'business/report/saleReport', 1, 0, 'C', '0', '0', 'business:report:sale', 'chart', 'admin', sysdate(), '', null, '销售明细报表菜单');
