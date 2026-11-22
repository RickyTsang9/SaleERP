-- =====================================================================
-- SaleERP Phase 3-5 Roadmap Completion Migration (2026-11-22)
-- 1. Add batch expiry date column (expire_date) to wms_stock and wms_inbound_item
-- 2. Seed realistic FMCG batch expiry dates for demo stock records
-- 3. Register automatic weekly operation snapshot Quartz job
-- =====================================================================

-- 1. Add expire_date to wms_stock if missing
SET @col_stock := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wms_stock' AND COLUMN_NAME = 'expire_date'
);
SET @sql_stock := IF(@col_stock = 0,
    'ALTER TABLE wms_stock ADD COLUMN expire_date DATE NULL COMMENT ''批次效期/到期日'' AFTER batch_no',
    'SELECT ''wms_stock.expire_date exists'''
);
PREPARE stmt_stock FROM @sql_stock;
EXECUTE stmt_stock;
DEALLOCATE PREPARE stmt_stock;

-- 2. Add expire_date to wms_inbound_item if missing
SET @col_inb := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wms_inbound_item' AND COLUMN_NAME = 'expire_date'
);
SET @sql_inb := IF(@col_inb = 0,
    'ALTER TABLE wms_inbound_item ADD COLUMN expire_date DATE NULL COMMENT ''批次效期/到期日'' AFTER batch_no',
    'SELECT ''wms_inbound_item.expire_date exists'''
);
PREPARE stmt_inb FROM @sql_inb;
EXECUTE stmt_inb;
DEALLOCATE PREPARE stmt_inb;

-- 3. Seed realistic FMCG expiry dates on demo food/beverage/daily-use stock batches
UPDATE wms_stock SET expire_date = DATE_ADD(CURDATE(), INTERVAL 12 DAY) WHERE product_id = 990001 AND expire_date IS NULL;
UPDATE wms_stock SET expire_date = DATE_ADD(CURDATE(), INTERVAL 75 DAY) WHERE product_id = 990002 AND expire_date IS NULL;
UPDATE wms_stock SET expire_date = DATE_ADD(CURDATE(), INTERVAL 180 DAY) WHERE product_id = 990004 AND expire_date IS NULL;
UPDATE wms_stock SET expire_date = DATE_ADD(CURDATE(), INTERVAL 5 DAY) WHERE product_id = 990005 AND expire_date IS NULL;

-- 4. Register automatic weekly operation snapshot Quartz job (idempotent)
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '经营周报快照自动归档', 'DEFAULT', 'bizOperationReportTask.generateWeeklySnapshot()', '0 0 8 ? * MON', '3', '1', '1', 'admin', NOW(), '每周一早8点自动汇总经营指标并归档周经营快照'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_job WHERE invoke_target = 'bizOperationReportTask.generateWeeklySnapshot()'
);
