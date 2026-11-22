-- ============================================================
-- SaleERP 第一批 + 第二批全链路业务闭环修复增量迁移脚本 (P0 + P1)
-- 日期: 2026-11-22
-- 特性: 100% 幂等，可重复安全执行
-- ============================================================

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS sp_add_col_if_absent_p0p1;
DELIMITER $$
CREATE PROCEDURE sp_add_col_if_absent_p0p1(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_ddl TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
    ) THEN
        SET @sql_stmt = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_ddl);
        PREPARE stmt FROM @sql_stmt;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 1. 出库明细与销售退货明细增加审核过账瞬时单位成本快照（修复 P0-08 历史毛利漂移）
CALL sp_add_col_if_absent_p0p1('wms_outbound_item', 'cost_price_snapshot', '`cost_price_snapshot` DECIMAL(18,4) NULL COMMENT ''出库审核瞬时单位成本快照'' AFTER `price`');
CALL sp_add_col_if_absent_p0p1('wms_sale_return_item', 'cost_price_snapshot', '`cost_price_snapshot` DECIMAL(18,4) NULL COMMENT ''退货审核瞬时单位成本快照'' AFTER `price`');

UPDATE wms_outbound_item oi
INNER JOIN md_product p ON p.product_id = oi.product_id
SET oi.cost_price_snapshot = IFNULL(p.cost_price, 0)
WHERE oi.cost_price_snapshot IS NULL;

UPDATE wms_sale_return_item sri
INNER JOIN md_product p ON p.product_id = sri.product_id
SET sri.cost_price_snapshot = IFNULL(p.cost_price, 0)
WHERE sri.cost_price_snapshot IS NULL;

-- 2. 销售订单与采购订单增加开票累计金额与开票状态（修复 P1-06 发票反写订单闭环）
CALL sp_add_col_if_absent_p0p1('wms_sale_order', 'invoiced_amount', '`invoiced_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT ''已开票金额'' AFTER `payment_status`');
CALL sp_add_col_if_absent_p0p1('wms_sale_order', 'invoice_status', '`invoice_status` VARCHAR(20) NOT NULL DEFAULT ''uninvoiced'' COMMENT ''开票状态(uninvoiced未开票 partial部分开票 invoiced已开票)'' AFTER `invoiced_amount`');

CALL sp_add_col_if_absent_p0p1('wms_purchase_order', 'invoiced_amount', '`invoiced_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT ''已收票金额'' AFTER `total_amount`');
CALL sp_add_col_if_absent_p0p1('wms_purchase_order', 'invoice_status', '`invoice_status` VARCHAR(20) NOT NULL DEFAULT ''uninvoiced'' COMMENT ''收票状态(uninvoiced未收票 partial部分收票 invoiced已收票)'' AFTER `invoiced_amount`');

-- 3. 采购退货与销售退货增加驳回原因字段（修复 P1-01 退货驳回流转）
CALL sp_add_col_if_absent_p0p1('wms_sale_return', 'reject_reason', '`reject_reason` VARCHAR(255) NULL COMMENT ''审核驳回原因'' AFTER `audit_time`');
CALL sp_add_col_if_absent_p0p1('wms_purchase_return', 'reject_reason', '`reject_reason` VARCHAR(255) NULL COMMENT ''审核驳回原因'' AFTER `audit_time`');

DROP PROCEDURE IF EXISTS sp_add_col_if_absent_p0p1;

-- 4. 资金账户收支流水明细表 fin_account_journal（修复 P1-04 资金流水、账户互转、日常收支、红字退款留痕）
CREATE TABLE IF NOT EXISTS `fin_account_journal` (
    `journal_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资金流水主键',
    `journal_no` VARCHAR(64) NOT NULL COMMENT '资金流水号',
    `account_id` BIGINT NOT NULL COMMENT '资金账户ID',
    `account_name` VARCHAR(100) NULL COMMENT '资金账户名称快照',
    `direction` VARCHAR(16) NOT NULL COMMENT '收支方向(in收入 out支出)',
    `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型(opening期初 receipt销售回款 receipt_void回款冲红 payment采购付款 payment_void付款冲红 sale_refund客户退款 purchase_refund供应商退款 transfer_in内部转入 transfer_out内部转出 other_income其他收入 other_expense日常费用)',
    `biz_id` BIGINT NULL COMMENT '关联业务主键',
    `biz_no` VARCHAR(64) NULL COMMENT '关联业务单号',
    `target_name` VARCHAR(128) NULL COMMENT '往来单位或对手账户',
    `amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '变动金额(正数)',
    `before_balance` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '变动前余额',
    `after_balance` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '变动后余额',
    `occur_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '业务发生时间',
    `remark` VARCHAR(500) NULL COMMENT '摘要备注',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '操作人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`journal_id`),
    UNIQUE KEY `uk_fin_account_journal_no` (`journal_no`),
    KEY `idx_fin_journal_account_time` (`account_id`, `occur_time`),
    KEY `idx_fin_journal_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金结算账户收支流水明细表';

-- 为已有结算账户初始化期初建账流水（仅首次执行）
INSERT INTO `fin_account_journal` (`journal_no`, `account_id`, `account_name`, `direction`, `biz_type`, `biz_id`, `biz_no`, `target_name`, `amount`, `before_balance`, `after_balance`, `occur_time`, `remark`, `create_by`, `create_time`)
SELECT
    CONCAT('ZJ-OPEN-', a.account_id),
    a.account_id,
    a.account_name,
    'in',
    'opening',
    a.account_id,
    a.account_code,
    '期初建账',
    IFNULL(a.current_balance, 0),
    0.00,
    IFNULL(a.current_balance, 0),
    IFNULL(a.create_time, NOW()),
    '系统账户初始化期初资金结余',
    'admin',
    NOW()
FROM `fin_account` a
WHERE NOT EXISTS (
    SELECT 1 FROM `fin_account_journal` j WHERE j.account_id = a.account_id
);

-- 5. 校正演示发票与真实销售单/采购单的关联，并反写订单开票累计金额与开票状态
UPDATE fin_invoice fi
INNER JOIN wms_sale_order so ON so.sale_order_id = fi.source_order_id
LEFT JOIN md_customer c ON c.customer_id = so.customer_id
SET fi.source_order_no = so.order_no,
    fi.target_id = so.customer_id,
    fi.target_name = IFNULL(c.customer_name, fi.target_name)
WHERE fi.invoice_direction = 'sale' AND fi.source_order_id IS NOT NULL;

UPDATE fin_invoice fi
INNER JOIN wms_purchase_order po ON po.order_no = 'PO20231024001'
LEFT JOIN md_supplier s ON s.supplier_id = po.supplier_id
SET fi.source_order_id = po.purchase_order_id,
    fi.source_order_no = po.order_no,
    fi.target_id = po.supplier_id,
    fi.target_name = IFNULL(s.supplier_name, fi.target_name)
WHERE fi.invoice_id = 3 AND fi.invoice_direction = 'purchase' AND fi.source_order_no = 'PO202604160001';

UPDATE fin_invoice fi
INNER JOIN wms_purchase_order po ON po.order_no = 'PO20231024002'
LEFT JOIN md_supplier s ON s.supplier_id = po.supplier_id
SET fi.source_order_id = po.purchase_order_id,
    fi.source_order_no = po.order_no,
    fi.target_id = po.supplier_id,
    fi.target_name = IFNULL(s.supplier_name, fi.target_name)
WHERE fi.invoice_id = 4 AND fi.invoice_direction = 'purchase' AND fi.source_order_no = 'PO202604160002';

UPDATE wms_sale_order so
SET so.invoiced_amount = IFNULL((
        SELECT SUM(i.total_amount) FROM fin_invoice i
        WHERE i.invoice_direction = 'sale' AND i.source_order_id = so.sale_order_id AND i.status = 'issued'
    ), 0.00),
    so.invoice_status = CASE
        WHEN IFNULL((SELECT SUM(i.total_amount) FROM fin_invoice i WHERE i.invoice_direction = 'sale' AND i.source_order_id = so.sale_order_id AND i.status = 'issued'), 0.00) <= 0 THEN 'uninvoiced'
        WHEN IFNULL((SELECT SUM(i.total_amount) FROM fin_invoice i WHERE i.invoice_direction = 'sale' AND i.source_order_id = so.sale_order_id AND i.status = 'issued'), 0.00) >= IFNULL(so.total_amount, 0) THEN 'invoiced'
        ELSE 'partial'
    END;

UPDATE wms_purchase_order po
SET po.invoiced_amount = IFNULL((
        SELECT SUM(i.total_amount) FROM fin_invoice i
        WHERE i.invoice_direction = 'purchase' AND i.source_order_id = po.purchase_order_id AND i.status = 'issued'
    ), 0.00),
    po.invoice_status = CASE
        WHEN IFNULL((SELECT SUM(i.total_amount) FROM fin_invoice i WHERE i.invoice_direction = 'purchase' AND i.source_order_id = po.purchase_order_id AND i.status = 'issued'), 0.00) <= 0 THEN 'uninvoiced'
        WHEN IFNULL((SELECT SUM(i.total_amount) FROM fin_invoice i WHERE i.invoice_direction = 'purchase' AND i.source_order_id = po.purchase_order_id AND i.status = 'issued'), 0.00) >= IFNULL(po.total_amount, 0) THEN 'invoiced'
        ELSE 'partial'
    END;

-- 6. 补齐缺失菜单与权限（修复 P0-10 销售明细报表 404、P1-09 库位导入权限、P1-10 库存冻结解冻权限）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES
(5340, '销售明细报表', 3028, 6, 'saleReport', 'business/report/saleReport', '', '', 1, 0, 'C', '0', '0', 'business:report:view', 'chart', 'admin', NOW(), '销售明细统计报表'),
(5341, '库位导入', 3004, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:import', '#', 'admin', NOW(), '库位批量导入权限'),
(5342, '库存冻结解冻', 3009, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:freeze', '#', 'admin', NOW(), '库存冻结与解冻操作权限')
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`),
    `parent_id` = VALUES(`parent_id`),
    `path` = VALUES(`path`),
    `component` = VALUES(`component`),
    `perms` = VALUES(`perms`),
    `visible` = VALUES(`visible`),
    `status` = VALUES(`status`);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.role_id, 5340 FROM `sys_role` r WHERE r.role_id IN (1, 2);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.role_id, 5341 FROM `sys_role` r WHERE r.role_id IN (1, 2);
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.role_id, 5342 FROM `sys_role` r WHERE r.role_id IN (1, 2);
