-- =============================================================================
-- SaleERP 全系统深度优化迁移脚本 (2026-11-24)
-- 涵盖：OPT-11 出库流水成本价修正、OPT-12 移动加权成本4位精度与销售明细快照、
--       OPT-13 客户账期天数、OPT-15 客户跟进Quartz提醒、OPT-21 库存流水bill_no持久化、
--       OPT-22 全库高频复合索引补齐与冗余索引清理（100% 幂等可重复执行）
-- =============================================================================

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS sp_saleerp_opt_20261124;
DELIMITER $$
CREATE PROCEDURE sp_saleerp_opt_20261124()
BEGIN
    -- 1. OPT-13: md_customer 新增 credit_days 账期天数
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'md_customer' AND column_name = 'credit_days'
    ) THEN
        ALTER TABLE md_customer ADD COLUMN credit_days INT DEFAULT NULL COMMENT '授信账期天数(0为现款现货)' AFTER credit_limit;
    END IF;

    UPDATE md_customer
       SET credit_days = CASE WHEN payment_method = 'cash' THEN 0 ELSE 30 END
     WHERE credit_days IS NULL;

    -- 2. OPT-12: md_product.cost_price 升级为 DECIMAL(18,4)
    ALTER TABLE md_product MODIFY COLUMN cost_price DECIMAL(18,4) DEFAULT 0.0000 COMMENT '移动加权平均成本价';

    -- 3. OPT-12: wms_sale_order_item 新增 cost_price_snapshot
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'wms_sale_order_item' AND column_name = 'cost_price_snapshot'
    ) THEN
        ALTER TABLE wms_sale_order_item ADD COLUMN cost_price_snapshot DECIMAL(18,4) DEFAULT NULL COMMENT '成本单价快照' AFTER amount;
    END IF;

    UPDATE wms_sale_order_item soi
      LEFT JOIN (
          SELECT oi.sale_order_item_id, AVG(oi.cost_price_snapshot) AS snap_cost
            FROM wms_outbound_item oi
            JOIN wms_outbound o ON o.outbound_id = oi.outbound_id
           WHERE o.status = 'audited' AND oi.cost_price_snapshot IS NOT NULL
           GROUP BY oi.sale_order_item_id
      ) ob ON ob.sale_order_item_id = soi.sale_order_item_id
      LEFT JOIN md_product mp ON mp.product_id = soi.product_id
       SET soi.cost_price_snapshot = COALESCE(ob.snap_cost, mp.cost_price, 0.0000)
     WHERE soi.cost_price_snapshot IS NULL;

    -- 4. OPT-21: wms_stock_log 新增 bill_no 并回填历史单号
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'wms_stock_log' AND column_name = 'bill_no'
    ) THEN
        ALTER TABLE wms_stock_log ADD COLUMN bill_no VARCHAR(64) DEFAULT NULL COMMENT '关联业务单据编号' AFTER bill_id;
    END IF;

    UPDATE wms_stock_log sl
      LEFT JOIN wms_inbound wi ON sl.bill_type = 'inbound' AND wi.inbound_id = sl.bill_id
      LEFT JOIN wms_outbound wo ON sl.bill_type = 'outbound' AND wo.outbound_id = sl.bill_id
      LEFT JOIN wms_sale_return sr ON sl.bill_type = 'sale_return' AND sr.sale_return_id = sl.bill_id
      LEFT JOIN wms_purchase_return pr ON sl.bill_type = 'purchase_return' AND pr.purchase_return_id = sl.bill_id
      LEFT JOIN wms_inventory_check ic ON sl.bill_type = 'inventory_check' AND ic.check_id = sl.bill_id
      LEFT JOIN wms_transfer tf ON sl.bill_type IN ('transfer', 'transfer_out', 'transfer_in', 'transfer_cancel') AND tf.transfer_id = sl.bill_id
      LEFT JOIN wms_sale_order so ON sl.bill_type IN ('sale_order', 'sale_order_rollback') AND so.sale_order_id = sl.bill_id
       SET sl.bill_no = COALESCE(wi.inbound_no, wo.outbound_no, sr.return_no, pr.return_no, ic.check_no, tf.transfer_no, so.order_no)
     WHERE sl.bill_no IS NULL OR sl.bill_no = '';

    -- 5. OPT-11: 修正历史出库库存流水误记销售价 -> 统一改为成本快照价
    UPDATE wms_stock_log sl
      JOIN (
          SELECT outbound_id, product_id, IFNULL(batch_no, '') AS batch_key, MAX(cost_price_snapshot) AS snap_cost
            FROM wms_outbound_item
           WHERE cost_price_snapshot IS NOT NULL
           GROUP BY outbound_id, product_id, IFNULL(batch_no, '')
      ) oi ON sl.bill_type = 'outbound'
          AND oi.outbound_id = sl.bill_id
          AND oi.product_id = sl.product_id
          AND oi.batch_key = IFNULL(sl.batch_no, '')
       SET sl.price = ROUND(oi.snap_cost, 2),
           sl.amount = ROUND(sl.quantity * oi.snap_cost, 2);

    -- 6. OPT-22: 补齐高频复合索引与唯一索引
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_transfer_item' AND index_name = 'idx_wms_transfer_item_transfer_id') THEN
        CREATE INDEX idx_wms_transfer_item_transfer_id ON wms_transfer_item (transfer_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_transfer_item' AND index_name = 'idx_wms_transfer_item_product_id') THEN
        CREATE INDEX idx_wms_transfer_item_product_id ON wms_transfer_item (product_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_sale_order_item' AND index_name = 'idx_wms_sale_order_item_product_order') THEN
        CREATE INDEX idx_wms_sale_order_item_product_order ON wms_sale_order_item (product_id, sale_order_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_purchase_order' AND index_name = 'uk_wms_purchase_order_no') THEN
        CREATE UNIQUE INDEX uk_wms_purchase_order_no ON wms_purchase_order (order_no);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'md_product' AND index_name = 'idx_md_product_bar_code') THEN
        CREATE INDEX idx_md_product_bar_code ON md_product (bar_code);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_stock_log' AND index_name = 'idx_stock_log_wh_time') THEN
        CREATE INDEX idx_stock_log_wh_time ON wms_stock_log (warehouse_id, create_time, stock_log_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_stock_log' AND index_name = 'idx_stock_log_prod_time') THEN
        CREATE INDEX idx_stock_log_prod_time ON wms_stock_log (product_id, create_time, stock_log_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_stock_log' AND index_name = 'idx_stock_log_bill_no') THEN
        CREATE INDEX idx_stock_log_bill_no ON wms_stock_log (bill_no);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'fin_account_journal' AND index_name = 'idx_fin_journal_occur_time_id') THEN
        CREATE INDEX idx_fin_journal_occur_time_id ON fin_account_journal (occur_time, journal_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'fin_account_journal' AND index_name = 'idx_fin_journal_dir_type_time') THEN
        CREATE INDEX idx_fin_journal_dir_type_time ON fin_account_journal (direction, biz_type, occur_time);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_sale_order' AND index_name = 'idx_wms_sale_order_status_audit_time') THEN
        CREATE INDEX idx_wms_sale_order_status_audit_time ON wms_sale_order (status, audit_time);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_sale_order' AND index_name = 'idx_wms_sale_order_status_invoice') THEN
        CREATE INDEX idx_wms_sale_order_status_invoice ON wms_sale_order (status, invoice_status);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_sale_order' AND index_name = 'idx_wms_sale_order_create_by') THEN
        CREATE INDEX idx_wms_sale_order_create_by ON wms_sale_order (create_by);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_stock' AND index_name = 'idx_wms_stock_wh_prod_expire') THEN
        CREATE INDEX idx_wms_stock_wh_prod_expire ON wms_stock (warehouse_id, product_id, expire_date);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_stock' AND index_name = 'idx_wms_stock_expire_date') THEN
        CREATE INDEX idx_wms_stock_expire_date ON wms_stock (expire_date);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'crm_customer_follow' AND index_name = 'idx_crm_follow_status_next_time') THEN
        CREATE INDEX idx_crm_follow_status_next_time ON crm_customer_follow (status, next_follow_time);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_sale_return' AND index_name = 'idx_wms_sale_return_order_status') THEN
        CREATE INDEX idx_wms_sale_return_order_status ON wms_sale_return (sale_order_id, status);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_purchase_return' AND index_name = 'idx_wms_purchase_return_order_status') THEN
        CREATE INDEX idx_wms_purchase_return_order_status ON wms_purchase_return (purchase_order_id, status);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_sale_return_item' AND index_name = 'idx_wms_sale_return_item_product_id') THEN
        CREATE INDEX idx_wms_sale_return_item_product_id ON wms_sale_return_item (product_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_inventory_check_item' AND index_name = 'idx_wms_check_item_product_id') THEN
        CREATE INDEX idx_wms_check_item_product_id ON wms_inventory_check_item (product_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'fin_receipt' AND index_name = 'idx_fin_receipt_status_payment_time') THEN
        CREATE INDEX idx_fin_receipt_status_payment_time ON fin_receipt (status, payment_time);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'fin_receipt' AND index_name = 'idx_fin_receipt_customer_payment_time') THEN
        CREATE INDEX idx_fin_receipt_customer_payment_time ON fin_receipt (customer_id, payment_time);
    END IF;

    -- 7. OPT-22: 清理重复冗余索引
    IF EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_sale_order' AND index_name = 'idx_wms_sale_order_order_no') THEN
        DROP INDEX idx_wms_sale_order_order_no ON wms_sale_order;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_purchase_order' AND index_name = 'idx_wms_purchase_order_status') THEN
        DROP INDEX idx_wms_purchase_order_status ON wms_purchase_order;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'wms_stock_log' AND index_name = 'idx_wms_stock_log_warehouse_id') THEN
        DROP INDEX idx_wms_stock_log_warehouse_id ON wms_stock_log;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'fin_receivable' AND index_name = 'idx_fin_receivable_customer_status') THEN
        DROP INDEX idx_fin_receivable_customer_status ON fin_receivable;
    END IF;

    -- 8. OPT-15: 预置客户跟进到期提醒 Quartz 定时任务
    INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
    VALUES (103, '客户跟进到期提醒推送', 'DEFAULT', 'crmCustomerFollowRemindTask.pushDueFollowReminder()', '0 30 8 * * ?', '3', '1', '0', 'admin', NOW(), '每日晨间自动推送今日到期与逾期未跟进客户提醒')
    ON DUPLICATE KEY UPDATE
        job_name = VALUES(job_name),
        invoke_target = VALUES(invoke_target),
        cron_expression = VALUES(cron_expression),
        status = '0';
END$$
DELIMITER ;

CALL sp_saleerp_opt_20261124();
DROP PROCEDURE IF EXISTS sp_saleerp_opt_20261124;
