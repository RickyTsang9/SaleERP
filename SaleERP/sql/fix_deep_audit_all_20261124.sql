-- ============================================================================
-- SaleERP Deep System Audit Full Fix Migration (2026-11-24)
-- Covers:
--   P0-01: fin_receivable / fin_payable nullable source_order_id & drop unique constraint blocking red-ink refund rows
--   P0-02: Fix non-admin business roles data_scope (was '2' with empty sys_role_dept -> 0 rows)
--   P0-03: Revoke system/monitor/tool admin escalation from role 2 (common); add Purchase (6) & Warehouse (7) roles;
--          rebuild complete least-privilege sys_role_menu matrices for roles 2..7
--   P0-04: Fix sys_menu 5340 (business:report:sale) and orphan parent_id on 5335 (3021 -> 3019)
--   P2-04: Upgrade purchase/payable/payment/transfer DECIMAL(10,2) columns to DECIMAL(18,2)
--   P2-09: Clean orphan locked_quantity, inconsistent demo order states (PO 102/103, OB 990006),
--          missing backup seed record (id=2), and activate/seed standard Quartz report jobs
-- ============================================================================

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- 1. P0-01 & P2-04: Schema fixes on fin_receivable, fin_payable, wms_purchase_order*, fin_payment, wms_transfer_item
-- ----------------------------------------------------------------------------
ALTER TABLE `fin_receivable`
  MODIFY COLUMN `sale_order_id` bigint DEFAULT NULL COMMENT '销售单编号（无源退货红字应收可为空）';

-- Drop uk_fin_receivable_sale_order_id if exists and ensure non-unique index idx_fin_receivable_sale_order_id exists
SET @has_uk := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fin_receivable' AND INDEX_NAME = 'uk_fin_receivable_sale_order_id'
);
SET @sql_drop_uk := IF(@has_uk > 0, 'ALTER TABLE `fin_receivable` DROP INDEX `uk_fin_receivable_sale_order_id`', 'SELECT 1');
PREPARE stmt_drop_uk FROM @sql_drop_uk;
EXECUTE stmt_drop_uk;
DEALLOCATE PREPARE stmt_drop_uk;

SET @has_idx := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fin_receivable' AND INDEX_NAME = 'idx_fin_receivable_sale_order_id'
);
SET @sql_add_idx := IF(@has_idx = 0, 'ALTER TABLE `fin_receivable` ADD INDEX `idx_fin_receivable_sale_order_id` (`sale_order_id`)', 'SELECT 1');
PREPARE stmt_add_idx FROM @sql_add_idx;
EXECUTE stmt_add_idx;
DEALLOCATE PREPARE stmt_add_idx;

ALTER TABLE `fin_payable`
  MODIFY COLUMN `purchase_order_id` bigint DEFAULT NULL COMMENT '采购订单ID（无源采购退货红字应付可为空）',
  MODIFY COLUMN `amount_due` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '应付金额',
  MODIFY COLUMN `amount_paid` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '已付金额',
  MODIFY COLUMN `remain_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '未付金额';

ALTER TABLE `fin_payment`
  MODIFY COLUMN `amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '付款金额';

ALTER TABLE `wms_purchase_order`
  MODIFY COLUMN `total_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '采购总金额';

ALTER TABLE `wms_purchase_order_item`
  MODIFY COLUMN `quantity` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '采购数量',
  MODIFY COLUMN `price` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '采购单价',
  MODIFY COLUMN `amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '明细金额';

ALTER TABLE `wms_transfer_item`
  MODIFY COLUMN `quantity` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '调拨数量';

-- ----------------------------------------------------------------------------
-- 2. P0-04: Fix sys_menu 5335 (orphan parent_id 3021 -> 3019) & 5340 (perms -> business:report:sale)
-- ----------------------------------------------------------------------------
UPDATE `sys_menu`
SET `parent_id` = 3019, `update_by` = 'admin', `update_time` = NOW()
WHERE `menu_id` = 5335;

UPDATE `sys_menu`
SET `perms` = 'business:report:sale', `update_by` = 'admin', `update_time` = NOW()
WHERE `menu_id` = 5340;

-- ----------------------------------------------------------------------------
-- 3. P0-02 & P0-03: Fix sys_role data_scope, add Purchase(6) & Warehouse(7), rebuild sys_role_menu
-- ----------------------------------------------------------------------------
UPDATE `sys_role` SET `data_scope` = '4', `remark` = '普通只读/协同角色（不含系统管理权限）', `update_by` = 'admin', `update_time` = NOW() WHERE `role_id` = 2;
UPDATE `sys_role` SET `data_scope` = '4', `remark` = '销售业务角色（本部门及以下业务数据）', `update_by` = 'admin', `update_time` = NOW() WHERE `role_id` = 3;
UPDATE `sys_role` SET `data_scope` = '1', `remark` = '业务经理角色（全公司业务审批与经营分析）', `update_by` = 'admin', `update_time` = NOW() WHERE `role_id` = 4;
UPDATE `sys_role` SET `data_scope` = '1', `remark` = '财务核算角色（全公司应收应付、资金发票与月结）', `update_by` = 'admin', `update_time` = NOW() WHERE `role_id` = 5;

INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
  (6, '采购角色', 'purchase', 6, '1', 1, 1, '0', '0', 'admin', NOW(), 'admin', NOW(), '采购业务角色（供应商、采购订单与退货制单）'),
  (7, '仓管角色', 'warehouse', 7, '1', 1, 1, '0', '0', 'admin', NOW(), 'admin', NOW(), '仓储作业角色（出入库、调拨、盘点与移动PDA作业）')
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `role_key` = VALUES(`role_key`),
  `data_scope` = VALUES(`data_scope`),
  `status` = '0',
  `del_flag` = '0',
  `remark` = VALUES(`remark`),
  `update_by` = 'admin',
  `update_time` = NOW();

-- Clean unused custom dept bindings for roles 2..7
DELETE FROM `sys_role_dept` WHERE `role_id` IN (2, 3, 4, 5, 6, 7);

-- Rebuild sys_role_menu for roles 2..7 cleanly (strict least privilege)
DELETE FROM `sys_role_menu` WHERE `role_id` IN (2, 3, 4, 5, 6, 7);

-- Role 2: 普通角色 (common) - NO system/monitor/tool admin perms, NO profit perm, safe read-only business viewer
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (2, 5300), (2, 5301),
  (2, 3023),
  (2, 3001), (2, 3101),
  (2, 3002), (2, 3201),
  (2, 5205), (2, 5206),
  (2, 5237), (2, 5238),
  (2, 3026), (2, 3013), (2, 4301),
  (2, 3027), (2, 3009), (2, 3901),
  (2, 3028), (2, 3019), (2, 4901), (2, 5340);

-- Role 3: 销售角色 (sale) - Customer CRM, Sale Order (no audit), Sale Return (no audit), Stock/AR read-only, Sale Report (masked cost)
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (3, 5300), (3, 5301),
  (3, 3023),
  (3, 3001), (3, 3101),
  (3, 3002), (3, 2472), (3, 3201), (3, 3202), (3, 3203), (3, 3205),
  (3, 5237), (3, 5238), (3, 5239), (3, 5240), (3, 5242),
  (3, 3026),
  (3, 3013), (3, 2470), (3, 4301), (3, 4302), (3, 4303), (3, 4304), (3, 4305), (3, 4307), (3, 4308), (3, 4310),
  (3, 3007), (3, 3701),
  (3, 3015), (3, 4501), (3, 4502), (3, 4503), (3, 4504), (3, 4505),
  (3, 3027), (3, 3009), (3, 3901),
  (3, 3028), (3, 3017), (3, 4701), (3, 4706), (3, 4707), (3, 3018), (3, 4801), (3, 5340);

-- Role 4: 经理角色 (manager) - All parent directories + cross-module audit + full executive dashboard & profit visibility
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (4, 5300), (4, 5301),
  (4, 3023),
  (4, 3001), (4, 3101), (4, 3105),
  (4, 3002), (4, 3201), (4, 3203), (4, 3205),
  (4, 5205), (4, 5206), (4, 5210),
  (4, 5237), (4, 5238), (4, 5242),
  (4, 3024), (4, 3003), (4, 3301), (4, 3004), (4, 3401),
  (4, 3025),
  (4, 5211), (4, 5212), (4, 5216), (4, 5217),
  (4, 3005), (4, 3501), (4, 3505), (4, 3506),
  (4, 3011), (4, 4101), (4, 4105), (4, 4106),
  (4, 3026),
  (4, 3013), (4, 2470), (4, 4301), (4, 4305), (4, 4306), (4, 4308),
  (4, 3007), (4, 3701), (4, 3705), (4, 3706),
  (4, 3015), (4, 4501), (4, 4505), (4, 4506),
  (4, 3027),
  (4, 3009), (4, 3901), (4, 3905),
  (4, 3010), (4, 4001), (4, 4002),
  (4, 3020), (4, 5001), (4, 5005), (4, 5006),
  (4, 5230), (4, 5231), (4, 5235), (4, 5236),
  (4, 3028),
  (4, 3017), (4, 4701), (4, 4705), (4, 4706), (4, 4707),
  (4, 3018), (4, 4801), (4, 4805),
  (4, 5218), (4, 5219), (4, 5223),
  (4, 5224), (4, 5225), (4, 5229),
  (4, 5310), (4, 5311), (4, 5315),
  (4, 5320),
  (4, 3019), (4, 4901), (4, 4902), (4, 4903), (4, 4904), (4, 5335),
  (4, 5340);

-- Role 5: 财务角色 (finance) - Full AR/AP/Receipt/Payment/Invoice/Account/PeriodClose + SO financeAudit + Report & Profit
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (5, 5300), (5, 5301),
  (5, 3023), (5, 3001), (5, 3101), (5, 3002), (5, 3201), (5, 5205), (5, 5206),
  (5, 3025), (5, 5211), (5, 5212), (5, 5217), (5, 3005), (5, 3501), (5, 3011), (5, 4101),
  (5, 3026), (5, 3013), (5, 2470), (5, 4301), (5, 4305), (5, 4308), (5, 4309), (5, 3007), (5, 3701), (5, 3015), (5, 4501),
  (5, 3027), (5, 3009), (5, 3901), (5, 3010), (5, 4001),
  (5, 3028),
  (5, 3017), (5, 2364), (5, 4701), (5, 4702), (5, 4703), (5, 4704), (5, 4705), (5, 4706), (5, 4707),
  (5, 3018), (5, 4801), (5, 4802), (5, 4803), (5, 4804), (5, 4805),
  (5, 5218), (5, 5219), (5, 5220), (5, 5221), (5, 5222), (5, 5223),
  (5, 5224), (5, 5225), (5, 5226), (5, 5227), (5, 5228), (5, 5229),
  (5, 5310), (5, 5311), (5, 5312), (5, 5313), (5, 5314), (5, 5315),
  (5, 5320), (5, 5321), (5, 5322),
  (5, 3019), (5, 4901), (5, 4902), (5, 4904), (5, 5335),
  (5, 5340),
  (5, 3022), (5, 5201), (5, 5202), (5, 5336);

-- Role 6: 采购角色 (purchase) - Product/Supplier maintenance + Purchase Order/Inbound/Return creation + Stock/AP read-only
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (6, 5300), (6, 5301),
  (6, 3023),
  (6, 3001), (6, 2471), (6, 3101), (6, 3102), (6, 3103), (6, 3105),
  (6, 5205), (6, 2473), (6, 5206), (6, 5207), (6, 5208), (6, 5210),
  (6, 3024), (6, 3003), (6, 3301), (6, 3004), (6, 3401),
  (6, 3025),
  (6, 5211), (6, 5212), (6, 5213), (6, 5214), (6, 5215), (6, 5217),
  (6, 3005), (6, 3501), (6, 3502), (6, 3503), (6, 3504), (6, 3505),
  (6, 3011), (6, 4101), (6, 4102), (6, 4103), (6, 4104), (6, 4105),
  (6, 3027), (6, 3009), (6, 3901), (6, 3010), (6, 4001),
  (6, 3028), (6, 5218), (6, 5219), (6, 5224), (6, 5225);

-- Role 7: 仓管角色 (warehouse) - Warehouse/Location + Inbound/Outbound/Return/Transfer/Check/Stock/PDA (no profit perm)
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (7, 5300), (7, 5301),
  (7, 3023), (7, 3001), (7, 3101), (7, 3002), (7, 3201), (7, 5205), (7, 5206),
  (7, 3024),
  (7, 3003), (7, 3301), (7, 3302), (7, 3303), (7, 3304), (7, 3305),
  (7, 3004), (7, 3401), (7, 3402), (7, 3403), (7, 3404), (7, 3405), (7, 5341),
  (7, 3025),
  (7, 5211), (7, 5212),
  (7, 3005), (7, 3501), (7, 3502), (7, 3503), (7, 3504), (7, 3505), (7, 3506),
  (7, 3011), (7, 4101), (7, 4102), (7, 4103), (7, 4104), (7, 4105), (7, 4106),
  (7, 3026),
  (7, 3013), (7, 2470), (7, 4301), (7, 4308),
  (7, 3007), (7, 3701), (7, 3702), (7, 3703), (7, 3704), (7, 3705), (7, 3706),
  (7, 3015), (7, 4501), (7, 4502), (7, 4503), (7, 4504), (7, 4505), (7, 4506),
  (7, 3027),
  (7, 3009), (7, 2474), (7, 3901), (7, 3902), (7, 3903), (7, 3904), (7, 3905), (7, 5342),
  (7, 3010), (7, 4001), (7, 4002),
  (7, 3020), (7, 5001), (7, 5002), (7, 5003), (7, 5004), (7, 5005), (7, 5006),
  (7, 5230), (7, 5231), (7, 5232), (7, 5233), (7, 5234), (7, 5235), (7, 5236),
  (7, 5330);

-- ----------------------------------------------------------------------------
-- 4. P2-09: Baseline demo data hygiene cleanup
-- ----------------------------------------------------------------------------
-- 4.1 Release 12.00 orphan legacy locked_quantity on wms_stock 990001..990005 (no unshipped lock_on_audit SO exists)
UPDATE `wms_stock`
SET `locked_quantity` = 0.00, `update_by` = 'admin', `update_time` = NOW()
WHERE `stock_id` IN (990001, 990002, 990003, 990004, 990005)
  AND `locked_quantity` > 0
  AND NOT EXISTS (
    SELECT 1 FROM `wms_sale_order` so
    WHERE so.`status` = 'audited'
      AND so.`stock_deduct_mode` = 'lock_on_audit'
      AND EXISTS (
        SELECT 1 FROM `wms_sale_order_item` soi
        WHERE soi.`sale_order_id` = so.`sale_order_id`
          AND soi.`product_id` = `wms_stock`.`product_id`
          AND IFNULL(soi.`quantity`, 0) > IFNULL((
            SELECT SUM(oi.`quantity`) FROM `wms_outbound_item` oi
            INNER JOIN `wms_outbound` ob ON ob.`outbound_id` = oi.`outbound_id`
            WHERE ob.`sale_order_id` = so.`sale_order_id` AND ob.`status` = 'audited' AND oi.`product_id` = soi.`product_id`
          ), 0)
      )
  );

-- 4.2 Fix PO 102/103 status ('0' pending -> '1' audited, since they already have payables, invoices & linked inbounds)
UPDATE `wms_purchase_order`
SET `status` = '1', `update_by` = 'admin', `update_time` = NOW()
WHERE `purchase_order_id` IN (102, 103) AND `status` = '0';

-- 4.3 Cancel inconsistent un-audited outbound 990006 pointing to un-audited SO 9
UPDATE `wms_outbound`
SET `status` = 'cancelled', `remark` = '基线数据清洗：原关联销售单SO20231026002尚未完成财务审核', `update_by` = 'admin', `update_time` = NOW()
WHERE `outbound_id` = 990006 AND `sale_order_id` = 9 AND `status` = 'submitted';

-- 4.4 Remove broken backup seed pointing to non-existent manual_20261122174122.json.gz
DELETE FROM `biz_data_backup` WHERE `backup_id` = 2 AND `file_path` = 'manual_20261122174122.json.gz';

-- 4.5 Activate weekly report snapshot job and seed daily/monthly report snapshot jobs
UPDATE `sys_job`
SET `status` = '0', `update_by` = 'admin', `update_time` = NOW()
WHERE `job_id` = 100 AND `invoke_target` = 'bizOperationReportTask.generateWeeklySnapshot()';

INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
  (101, '经营日报快照自动归档', 'DEFAULT', 'bizOperationReportTask.generateDailySnapshot()', '0 55 23 * * ?', '3', '1', '0', 'admin', NOW(), 'admin', NOW(), '每日23:55自动归档经营日报快照'),
  (102, '经营月报快照自动归档', 'DEFAULT', 'bizOperationReportTask.generateMonthlySnapshot()', '0 0 8 1 * ?', '3', '1', '0', 'admin', NOW(), 'admin', NOW(), '每月1日08:00自动归档经营月报快照')
ON DUPLICATE KEY UPDATE
  `job_name` = VALUES(`job_name`),
  `invoke_target` = VALUES(`invoke_target`),
  `cron_expression` = VALUES(`cron_expression`),
  `status` = '0',
  `update_by` = 'admin',
  `update_time` = NOW();
